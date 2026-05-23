package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollEntity;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollState;
import com.sejinzx.enrollmentSystem.enroll.repository.EnrollRepository;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
class EnrollServiceTest {

    @Autowired
    private EnrollService enrollService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    /*
     * 수강 신청 성공 테스트
     * 예상 결과: 신청 성공
     */
    @Test
    void createEnroll_success_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("spring")
                        .classContent("backend")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(10))
                        .user(creator)
                        .build()
        );

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
     * 정원 초과 테스트
     * 예상 결과: 정원 초과 예외 발생
     */
    @Test
    void createEnroll_capacity_fail_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("full")
                        .classContent("full")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(1)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .user(creator)
                        .build()
        );

        classEntity.increaseCurrApps();
        classRepository.save(classEntity);

        // when
        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> enrollService.createEnroll(
                                classEntity.getClassSeq(),
                                student.getUserId()
                        )
                );

        // then
        Assertions.assertEquals(
                "정원 초과",
                exception.getMessage()
        );

        System.out.println("정원 초과 예외 발생");
    }

    /*
     * 중복 신청 테스트
     * 예상 결과: 이미 신청한 강의 예외 발생
     */
    @Test
    void createEnroll_duplicate_fail_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("java")
                        .classContent("java")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .user(creator)
                        .build()
        );

        enrollRepository.save(
                EnrollEntity.builder()
                        .user(student)
                        .classEntity(classEntity)
                        .enrollState(EnrollState.PENDING)
                        .build()
        );

        // when
        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> enrollService.createEnroll(
                                classEntity.getClassSeq(),
                                student.getUserId()
                        )
                );

        // then
        Assertions.assertEquals(
                "이미 신청한 강의",
                exception.getMessage()
        );

        System.out.println("중복 신청 예외 발생");
    }

    /*
     * 수강 취소 성공 테스트
     * 예상 결과: 취소 성공
     */
    @Test
    void deleteEnroll_success_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("cancel")
                        .classContent("cancel")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .user(creator)
                        .build()
        );

        EnrollEntity enrollEntity = enrollRepository.save(
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
     * 결제 후 3일 이후 취소 실패 테스트
     * 예상 결과: 취소 불가 예외 발생
     */
    @Test
    void deleteEnroll_after3days_fail_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("pay")
                        .classContent("pay")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .user(creator)
                        .build()
        );

        EnrollEntity enrollEntity = enrollRepository.save(
                EnrollEntity.builder()
                        .user(student)
                        .classEntity(classEntity)
                        .enrollState(EnrollState.CONFIRMED)
                        .build()
        );

        enrollEntity.changeUpdateDate(
                LocalDate.now().minusDays(10)
        );

        // when
        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> enrollService.deleteEnroll(
                                enrollEntity.getEnrollSeq(),
                                student.getUserId()
                        )
                );

        // then
        Assertions.assertEquals(
                "결제 후 3일 지나 취소 불가",
                exception.getMessage()
        );

        System.out.println("3일 이후 취소 실패");
    }

    /*
     * 재신청 성공 테스트
     * 예상 결과: CANCELLED -> PENDING 변경
     */
    @Test
    void reEnroll_success_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("re")
                        .classContent("re")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classState(ClassState.OPEN)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .user(creator)
                        .build()
        );

        enrollRepository.save(
                EnrollEntity.builder()
                        .user(student)
                        .classEntity(classEntity)
                        .enrollState(EnrollState.CANCELLED)
                        .build()
        );

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

        System.out.println("재신청 성공");
    }
}
