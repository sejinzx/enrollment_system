package com.sejinzx.enrollmentSystem.enroll.kafka;

import com.sejinzx.enrollmentSystem.KafkaMySqlContainerTest;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.enroll.repository.EnrollRepository;
import com.sejinzx.enrollmentSystem.enroll.service.EnrollService;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaEnrollmentIntegrationTest
        extends KafkaMySqlContainerTest {

    @Autowired
    private EnrollService enrollService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Kafka 수강 신청 메시지가 DB에 정상 반영된다")
    void kafkaEnrollmentSuccess() {

        // given
        String suffix =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator-" + suffix)
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student-" + suffix)
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("Kafka Test Class")
                        .classContent("Kafka Integration Test")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDateTime.now())
                        .classEndDate(LocalDateTime.now().plusDays(10))
                        .user(creator)
                        .build()
        );

        Long classSeq = classEntity.getClassSeq();

        // when
        // 실제 Kafka Producer를 통해 메시지 발행
        enrollService.requestEnroll(
                classSeq,
                student.getUserId()
        );

        // then
        // Consumer의 비동기 처리 완료까지 대기
        await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    long enrollCount =
                            enrollRepository
                                    .countByClassEntity_ClassSeq(
                                            classSeq
                                    );

                    ClassEntity resultClass =
                            classRepository
                                    .findById(classSeq)
                                    .orElseThrow();

                    assertThat(enrollCount)
                            .isEqualTo(1);

                    assertThat(resultClass.getClassCurrApps())
                            .isEqualTo(1);
                });
    }
}