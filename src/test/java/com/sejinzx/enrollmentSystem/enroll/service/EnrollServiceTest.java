package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.enroll.dto.ResponseGetEnroll;
import com.sejinzx.enrollmentSystem.enroll.dto.ResponseGetUserEnrollClass;
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
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     * 수강 신청 테스트
     */
    @Test
    void createEnroll_test() {

        // given

        // 강사 생성
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        // 학생 생성
        UserEntity student = userRepository.save(
                UserEntity.builder()
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        // 강의 생성
        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("Spring")
                        .classContent("Backend")
                        .classPrice(BigDecimal.valueOf(10000))
                        .classMaxCap(10)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .classState(ClassState.OPEN)
                        .user(creator)
                        .build()
        );

        // when
        Long enrollSeq = enrollService.createEnroll(
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

        Assertions.assertEquals(
                "student",
                result.getUser().getUserId()
        );
    }

    /*
     * 수강 신청 취소 테스트
     */
    @Test
    void deleteEnroll_test() {

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
                        .classTitle("Java")
                        .classContent("Java 강의")
                        .classPrice(BigDecimal.valueOf(5000))
                        .classMaxCap(20)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(10))
                        .classState(ClassState.OPEN)
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
    }

    /*
     * 내 수강 신청 목록 조회 테스트
     */
    @Test
    void getMyListEnroll_test() {

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

        // 강의 20개 생성
        for (int i = 1; i <= 20; i++) {

            ClassEntity classEntity = classRepository.save(
                    ClassEntity.builder()
                            .classTitle("class" + i)
                            .classContent("content")
                            .classPrice(BigDecimal.valueOf(1000))
                            .classMaxCap(20)
                            .classStartDate(LocalDate.now())
                            .classEndDate(LocalDate.now().plusDays(5))
                            .classState(ClassState.OPEN)
                            .user(creator)
                            .build()
            );

            enrollRepository.save(
                    EnrollEntity.builder()
                            .user(student)
                            .classEntity(classEntity)
                            .enrollState(EnrollState.CONFIRMED)
                            .build()
            );
        }

        // when
        Page<ResponseGetEnroll> result =
                enrollService.getMyListEnroll(
                        0,
                        10,
                        student.getUserId()
                );

        // then
        Assertions.assertEquals(
                20,
                result.getTotalElements()
        );

        Assertions.assertEquals(
                10,
                result.getContent().size()
        );
    }

    /*
     * 결제 완료 테스트
     */
    @Test
    void payedEnroll_test() {

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
                        .classTitle("Spring")
                        .classContent("content")
                        .classPrice(BigDecimal.valueOf(10000))
                        .classMaxCap(20)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .classState(ClassState.OPEN)
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
        enrollService.payedEnroll(
                enrollEntity.getEnrollSeq(),
                student.getUserId()
        );

        // then
        EnrollEntity result =
                enrollRepository.findById(
                        enrollEntity.getEnrollSeq()
                ).orElseThrow();

        Assertions.assertEquals(
                EnrollState.CONFIRMED,
                result.getEnrollState()
        );
    }

    /*
     * 강의 별 수강생 목록 조회 테스트
     */
    @Test
    void getClassEnrollUserList_test() {

        // given

        // 강사 생성
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        // 강의 생성
        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("Spring Boot")
                        .classContent("백엔드")
                        .classPrice(BigDecimal.valueOf(10000))
                        .classMaxCap(30)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(10))
                        .classState(ClassState.OPEN)
                        .user(creator)
                        .build()
        );

        // 학생 20명 생성
        for (int i = 1; i <= 20; i++) {

            UserEntity student = userRepository.save(
                    UserEntity.builder()
                            .userId("student" + i)
                            .userPw("1234")
                            .userType(UserType.CLASSMATE)
                            .build()
            );

            enrollRepository.save(
                    EnrollEntity.builder()
                            .user(student)
                            .classEntity(classEntity)
                            .enrollState(EnrollState.CONFIRMED)
                            .build()
            );
        }

        // when
        Page<ResponseGetUserEnrollClass> result =
                enrollService.getClassEnrollUserList(
                        0,
                        10,
                        classEntity.getClassSeq(),
                        creator.getUserId()
                );

        // then
        Assertions.assertEquals(
                20,
                result.getTotalElements()
        );

        Assertions.assertEquals(
                10,
                result.getContent().size()
        );

        System.out.println("===== 수강생 목록 =====");

        result.forEach(res -> {
            System.out.println(
                    "enrollSeq = " + res.getEnrollSeq()
            );

            System.out.println(
                    "userId = " + res.getUserId()
            );
        });
    }
}
