package com.sejinzx.enrollmentSystem.enroll.kafka;

import com.sejinzx.enrollmentSystem.KafkaMySqlContainerTest;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.enroll.dto.EnrollmentRequestedEvent;
import com.sejinzx.enrollmentSystem.enroll.repository.EnrollRepository;
import com.sejinzx.enrollmentSystem.enroll.service.EnrollService;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaIdempotencyIntegrationTest extends KafkaMySqlContainerTest {

    @Autowired
    private EnrollmentProducer enrollmentProducer;

    @SpyBean
    private EnrollService enrollService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("동일한 수강 신청 메시지가 중복 전달되어도 한 번만 반영된다")
    void duplicateMessageIsIdempotent() {

        // given
        String suffix =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        UserEntity creator =
                userRepository.save(
                        UserEntity.builder()
                                .userId("creator-" + suffix)
                                .userPw("1234")
                                .userType(UserType.CREATOR)
                                .build()
                );

        UserEntity student =
                userRepository.save(
                        UserEntity.builder()
                                .userId("student-" + suffix)
                                .userPw("1234")
                                .userType(UserType.CLASSMATE)
                                .build()
                );

        ClassEntity classEntity =
                classRepository.save(
                        ClassEntity.builder()
                                .classTitle("Idempotency Test Class")
                                .classContent("Kafka duplicate message test")
                                .classPrice(BigDecimal.valueOf(1000))
                                .classMaxCap(10)
                                .classState(ClassState.OPEN)
                                .classStartDate(LocalDateTime.now())
                                .classEndDate(LocalDateTime.now().plusDays(10))
                                .user(creator)
                                .build()
                );

        Long classSeq = classEntity.getClassSeq();
        String userId = student.getUserId();

        EnrollmentRequestedEvent event =
                new EnrollmentRequestedEvent(
                        userId,
                        classSeq,
                        System.currentTimeMillis()
                );

        // when
        // 동일한 수강 신청을 Kafka에 두 번 전달
        enrollmentProducer.sendRequest(event);
        enrollmentProducer.sendRequest(event);

        // 두 메시지가 Consumer까지 전달될 때까지 대기
        verify(
                enrollService,
                timeout(10000).atLeast(2)
        ).processEnroll(
                classSeq,
                userId
        );

        // then
        long enrollCount =
                enrollRepository
                        .countByClassEntity_ClassSeq(classSeq);

        ClassEntity resultClass =
                classRepository
                        .findById(classSeq)
                        .orElseThrow();

        // 신청 데이터는 한 건만 존재
        assertThat(enrollCount)
                .isEqualTo(1);

        // 신청 인원 역시 한 명만 증가
        assertThat(resultClass.getClassCurrApps())
                .isEqualTo(1);
    }
}