package com.sejinzx.enrollmentSystem.enroll.kafka;

import com.sejinzx.enrollmentSystem.KafkaMySqlContainerTest;
import com.sejinzx.enrollmentSystem.enroll.dto.EnrollmentRequestedEvent;
import com.sejinzx.enrollmentSystem.enroll.service.EnrollService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaRetryIntegrationTest
        extends KafkaMySqlContainerTest {

    @Autowired
    private EnrollmentProducer enrollmentProducer;

    @MockBean
    private EnrollService enrollService;

    @Test
    @DisplayName("DB 일시적 장애 발생 시 재시도 후 성공")
    void retrySuccess() {

        // given
        Long classSeq = 100L;

        String userId =
                "retry-" + UUID.randomUUID();

        // 첫 번째 실패
        // 두 번째 실패
        // 세 번째 성공
        doThrow(
                new DataAccessResourceFailureException(
                        "DB temporary error"
                )
        )
                .doThrow(
                        new DataAccessResourceFailureException(
                                "DB temporary error"
                        )
                )
                .doReturn(1L)
                .when(enrollService)
                .processEnroll(
                        eq(classSeq),
                        eq(userId)
                );

        // when
        enrollmentProducer.sendRequest(
                new EnrollmentRequestedEvent(
                        userId,
                        classSeq,
                        System.currentTimeMillis()
                )
        );

        // then
        await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    verify(
                            enrollService,
                            times(3)
                    ).processEnroll(
                            classSeq,
                            userId
                    );
                });
    }

    @Test
    @DisplayName("재시도 모두 실패하면 DLT로 이동")
    void retryFailToDlt() {

        // given
        Long classSeq = 200L;

        String userId =
                "dlt-" + UUID.randomUUID();

        // 모든 실행에서 DB 장애 발생
        doThrow(
                new DataAccessResourceFailureException(
                        "DB connection failed"
                )
        )
                .when(enrollService)
                .processEnroll(
                        eq(classSeq),
                        eq(userId)
                );

        // DLT 메시지 확인용 Consumer 설정
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers(),

                ConsumerConfig.GROUP_ID_CONFIG,
                "dlt-test-" + UUID.randomUUID(),

                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest",

                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,

                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ByteArrayDeserializer.class
        );

        try (
                KafkaConsumer<String, byte[]> consumer =
                        new KafkaConsumer<>(props)
        ) {

            consumer.subscribe(
                    List.of("enrollment-request-dlt")
            );

            // DLT Consumer 파티션 할당 대기
            long assignmentDeadline =
                    System.nanoTime()
                            + TimeUnit.SECONDS.toNanos(10);

            while (
                    consumer.assignment().isEmpty()
                            && System.nanoTime() < assignmentDeadline
            ) {
                consumer.poll(
                        Duration.ofMillis(200)
                );
            }

            assertThat(consumer.assignment()).isNotEmpty();

            // when
            enrollmentProducer.sendRequest(
                    new EnrollmentRequestedEvent(
                            userId,
                            classSeq,
                            System.currentTimeMillis()
                    )
            );

            ConsumerRecord<String, byte[]> dltRecord = null;

            long deadline =
                    System.nanoTime()
                            + TimeUnit.SECONDS.toNanos(20);

            // DLT 메시지가 도착할 때까지 반복 조회
            while (System.nanoTime() < deadline) {

                var records =
                        consumer.poll(
                                Duration.ofMillis(200)
                        );

                for (var record : records) {

                    if (classSeq.toString()
                            .equals(record.key())) {

                        dltRecord = record;
                        break;
                    }
                }

                if (dltRecord != null) {
                    break;
                }
            }

            verify(
                    enrollService,
                    timeout(10000).times(3)
            ).processEnroll(
                    classSeq,
                    userId
            );

            // then
            assertThat(dltRecord)
                    .isNotNull();

            assertThat(dltRecord.topic())
                    .isEqualTo(
                            "enrollment-request-dlt"
                    );

            assertThat(dltRecord.value())
                    .isNotEmpty();
        }
    }
}