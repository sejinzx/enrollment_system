package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.MySqlContainerTest;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollEntity;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollState;
import com.sejinzx.enrollmentSystem.enroll.repository.EnrollRepository;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnrollServiceTest extends MySqlContainerTest {

    @Autowired
    private EnrollService enrollService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @Test
    @DisplayName("수강 신청 성공")
    void processEnroll_success() {

        // given
        UserEntity creator = createUser("creator1", UserType.CREATOR);
        UserEntity student = createUser("student1", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 10);

        // when
        Long enrollSeq = enrollService.processEnroll(
                classEntity.getClassSeq(),
                student.getUserId()
        );

        // then
        EnrollEntity result = enrollRepository.findById(enrollSeq)
                .orElseThrow();

        ClassEntity updatedClass = classRepository.findById(
                classEntity.getClassSeq()
        ).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        EnrollState.PENDING,
                        result.getEnrollState()
                ),
                () -> Assertions.assertEquals(
                        student.getUserSeq(),
                        result.getUser().getUserSeq()
                ),
                () -> Assertions.assertEquals(
                        classEntity.getClassSeq(),
                        result.getClassEntity().getClassSeq()
                ),
                () -> Assertions.assertEquals(
                        1,
                        updatedClass.getClassCurrApps()
                )
        );
    }

    @Test
    @DisplayName("정원이 가득 찬 강의 신청 시 실패")
    void processEnroll_capacityFull_fail() {

        // given
        UserEntity creator = createUser("creator2", UserType.CREATOR);
        UserEntity student = createUser("student2", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 1);

        classEntity.changeCurrApps(1);
        classRepository.saveAndFlush(classEntity);

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> enrollService.processEnroll(
                        classEntity.getClassSeq(),
                        student.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.CLASS_CAPACITY_FULL,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("이미 신청한 강의 중복 신청 시 실패")
    void processEnroll_duplicate_fail() {

        // given
        UserEntity creator = createUser("creator3", UserType.CREATOR);
        UserEntity student = createUser("student3", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 10);

        createEnroll(
                student,
                classEntity,
                EnrollState.PENDING
        );

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> enrollService.processEnroll(
                        classEntity.getClassSeq(),
                        student.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.DUPLICATE_ENROLL,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("취소한 수강 신청 재신청 시 성공")
    void processEnroll_cancelled_reEnroll_success() {

        // given
        UserEntity creator = createUser("creator4", UserType.CREATOR);
        UserEntity student = createUser("student4", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 10);

        EnrollEntity enrollEntity = createEnroll(
                student,
                classEntity,
                EnrollState.CANCELLED
        );

        // when
        Long resultEnrollSeq = enrollService.processEnroll(
                classEntity.getClassSeq(),
                student.getUserId()
        );

        // then
        EnrollEntity result = enrollRepository.findById(
                enrollEntity.getEnrollSeq()
        ).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        enrollEntity.getEnrollSeq(),
                        resultEnrollSeq
                ),
                () -> Assertions.assertEquals(
                        EnrollState.PENDING,
                        result.getEnrollState()
                )
        );
    }

    @Test
    @DisplayName("결제 전 수강 신청 취소 성공")
    void deleteEnroll_pending_success() {

        // given
        UserEntity creator = createUser("creator5", UserType.CREATOR);
        UserEntity student = createUser("student5", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 10);

        EnrollEntity enrollEntity = createEnroll(
                student,
                classEntity,
                EnrollState.PENDING
        );

        // when
        Long resultEnrollSeq = enrollService.deleteEnroll(
                enrollEntity.getEnrollSeq(),
                student.getUserId()
        );

        // then
        EnrollEntity result = enrollRepository.findById(
                enrollEntity.getEnrollSeq()
        ).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        enrollEntity.getEnrollSeq(),
                        resultEnrollSeq
                ),
                () -> Assertions.assertEquals(
                        EnrollState.CANCELLED,
                        result.getEnrollState()
                )
        );
    }

    @Test
    @DisplayName("다른 사용자의 수강 신청 취소 시 실페")
    void deleteEnroll_otherUser_fail() {

        // given
        UserEntity creator = createUser("creator6", UserType.CREATOR);
        UserEntity student = createUser("student6", UserType.CLASSMATE);
        UserEntity otherStudent = createUser("student7", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 10);

        EnrollEntity enrollEntity = createEnroll(
                student,
                classEntity,
                EnrollState.PENDING
        );

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> enrollService.deleteEnroll(
                        enrollEntity.getEnrollSeq(),
                        otherStudent.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.FORBIDDEN,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("결제 후 3일이 지난 수강 신청 취소 시 실패")
    void deleteEnroll_confirmedAfterThreeDays_fail() {

        // given
        UserEntity creator = createUser("creator7", UserType.CREATOR);
        UserEntity student = createUser("student8", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 10);

        EnrollEntity enrollEntity = createEnroll(
                student,
                classEntity,
                EnrollState.CONFIRMED
        );

        ReflectionTestUtils.setField(
                enrollEntity,
                "enrollUpdateDate",
                LocalDateTime.now().minusDays(4)
        );

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> enrollService.deleteEnroll(
                        enrollEntity.getEnrollSeq(),
                        student.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.CANCEL_PERIOD_EXPIRED,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("존재하지 않는 수강 신청 조회 시 실패")
    void getEnroll_notFound_fail() {

        // given
        Long notExistEnrollSeq = 999999L;

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> enrollService.getEnroll(notExistEnrollSeq)
        );

        // then
        Assertions.assertEquals(
                ErrorCode.ENROLL_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("결제 후 수강 신청 상태 확정 변경")
    void payedEnroll_success() {

        // given
        UserEntity creator = createUser("creator8", UserType.CREATOR);
        UserEntity student = createUser("student9", UserType.CLASSMATE);
        ClassEntity classEntity = createClass(creator, 10);

        EnrollEntity enrollEntity = createEnroll(
                student,
                classEntity,
                EnrollState.PENDING
        );

        // when
        Long resultEnrollSeq = enrollService.payedEnroll(
                enrollEntity.getEnrollSeq(),
                student.getUserId()
        );

        // then
        EnrollEntity result = enrollRepository.findById(
                enrollEntity.getEnrollSeq()
        ).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        enrollEntity.getEnrollSeq(),
                        resultEnrollSeq
                ),
                () -> Assertions.assertEquals(
                        EnrollState.CONFIRMED,
                        result.getEnrollState()
                )
        );
    }

    private UserEntity createUser(
            String userId,
            UserType userType
    ) {
        return userRepository.save(
                UserEntity.builder()
                        .userId(userId)
                        .userPw("1234")
                        .userType(userType)
                        .build()
        );
    }

    private ClassEntity createClass(
            UserEntity creator,
            int maxCap
    ) {
        return classRepository.save(
                ClassEntity.builder()
                        .classTitle("test class")
                        .classContent("content")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(maxCap)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDateTime.now())
                        .classEndDate(LocalDateTime.now().plusDays(10))
                        .user(creator)
                        .build()
        );
    }

    private EnrollEntity createEnroll(
            UserEntity student,
            ClassEntity classEntity,
            EnrollState enrollState
    ) {
        return enrollRepository.save(
                EnrollEntity.builder()
                        .user(student)
                        .classEntity(classEntity)
                        .enrollState(enrollState)
                        .build()
        );
    }
}