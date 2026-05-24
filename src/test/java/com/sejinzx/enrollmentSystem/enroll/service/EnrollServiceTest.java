package com.sejinzx.enrollmentSystem.enroll.service;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnrollServiceTest {

    @Autowired
    private EnrollService enrollService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    /*
     * 수강 신청 성공
     */
    @Test
    void createEnroll_success_test() {

        // given
        UserEntity creator = createCreator("creator1");
        UserEntity student = createStudent("student1");

        ClassEntity classEntity =
                createClassEntity(creator, 10);

        // when
        Long enrollSeq =
                enrollService.createEnroll(
                        classEntity.getClassSeq(),
                        student.getUserId()
                );

        // then
        EnrollEntity result =
                enrollRepository.findById(enrollSeq)
                        .orElseThrow();

        Assertions.assertEquals(
                EnrollState.PENDING,
                result.getEnrollState()
        );

        System.out.println("수강 신청 성공");
    }

    /*
     * 정원 초과 실패
     */
    @Test
    void createEnroll_capacity_fail_test() {

        // given
        UserEntity creator = createCreator("creator2");
        UserEntity student = createStudent("student2");

        ClassEntity classEntity =
                createClassEntity(creator, 1);

        classEntity.changeCurrApps(1);

        classRepository.saveAndFlush(classEntity);

        // when
        BusinessException exception =
                Assertions.assertThrows(
                        BusinessException.class,
                        () -> enrollService.createEnroll(
                                classEntity.getClassSeq(),
                                student.getUserId()
                        )
                );

        // then
        Assertions.assertEquals(
                ErrorCode.CLASS_CAPACITY_FULL,
                exception.getErrorCode()
        );

        System.out.println("정원 초과 실패");
    }

    /*
     * 중복 신청 실패
     */
    @Test
    void createEnroll_duplicate_fail_test() {

        // given
        UserEntity creator = createCreator("creator3");
        UserEntity student = createStudent("student3");

        ClassEntity classEntity =
                createClassEntity(creator, 10);

        enrollRepository.save(
                EnrollEntity.builder()
                        .user(student)
                        .classEntity(classEntity)
                        .enrollState(EnrollState.PENDING)
                        .build()
        );

        // when
        BusinessException exception =
                Assertions.assertThrows(
                        BusinessException.class,
                        () -> enrollService.createEnroll(
                                classEntity.getClassSeq(),
                                student.getUserId()
                        )
                );

        // then
        Assertions.assertEquals(
                ErrorCode.DUPLICATE_ENROLL,
                exception.getErrorCode()
        );

        System.out.println("중복 신청 실패");
    }

    /*
     * 수강 취소 성공
     */
    @Test
    void deleteEnroll_success_test() {

        // given
        UserEntity creator = createCreator("creator4");
        UserEntity student = createStudent("student4");

        ClassEntity classEntity =
                createClassEntity(creator, 10);

        EnrollEntity enrollEntity =
                enrollRepository.save(
                        EnrollEntity.builder()
                                .user(student)
                                .classEntity(classEntity)
                                .enrollState(EnrollState.PENDING)
                                .build()
                );

        // when
        enrollService.deleteEnroll(
                enrollEntity.getEnrollSeq(),
                student.getUserId()
        );

        // then
        EnrollEntity result =
                enrollRepository.findById(
                        enrollEntity.getEnrollSeq()
                ).orElseThrow();

        Assertions.assertEquals(
                EnrollState.CANCELLED,
                result.getEnrollState()
        );

        System.out.println("수강 취소 성공");
    }

    /*
     * 결제 후 3일 이후 취소 실패
     */
    @Test
    void deleteEnroll_after3days_fail_test() {

        // given
        UserEntity creator = createCreator("creator5");
        UserEntity student = createStudent("student5");

        ClassEntity classEntity =
                createClassEntity(creator, 10);

        EnrollEntity enrollEntity =
                enrollRepository.save(
                        EnrollEntity.builder()
                                .user(student)
                                .classEntity(classEntity)
                                .enrollState(EnrollState.CONFIRMED)
                                .build()
                );

        ReflectionTestUtils.setField(
                enrollEntity,
                "enrollUpdateDate",
                LocalDate.now().minusDays(4)
        );

        // when
        BusinessException exception =
                Assertions.assertThrows(
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

        System.out.println("3일 이후 취소 실패");
    }

    /*
     * 취소 후 재신청 성공
     */
    @Test
    void createEnroll_reEnroll_success_test() {

        // given
        UserEntity creator = createCreator("creator6");
        UserEntity student = createStudent("student6");

        ClassEntity classEntity =
                createClassEntity(creator, 10);

        EnrollEntity enrollEntity =
                enrollRepository.save(
                        EnrollEntity.builder()
                                .user(student)
                                .classEntity(classEntity)
                                .enrollState(EnrollState.CANCELLED)
                                .build()
                );

        // when
        enrollService.createEnroll(
                classEntity.getClassSeq(),
                student.getUserId()
        );

        // then
        EnrollEntity result =
                enrollRepository.findById(
                        enrollEntity.getEnrollSeq()
                ).orElseThrow();

        Assertions.assertEquals(
                EnrollState.PENDING,
                result.getEnrollState()
        );

        System.out.println("재신청 성공");
    }

    /*
     * Creator 생성
     */
    private UserEntity createCreator(String userId) {

        return userRepository.save(
                UserEntity.builder()
                        .userId(userId)
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );
    }

    /*
     * Student 생성
     */
    private UserEntity createStudent(String userId) {

        return userRepository.save(
                UserEntity.builder()
                        .userId(userId)
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );
    }

    /*
     * 클래스 생성
     */
    private ClassEntity createClassEntity(
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
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(10))
                        .user(creator)
                        .build()
        );
    }
}
