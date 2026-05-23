package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
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
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollRepository enrollRepository;

    @Test
    void sync_text() throws Exception {

        // given

        // 강의 생성
        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("테스트")
                        .classContent("테스트")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(1))
                        .classState(ClassState.DRAFT)
                        .build()
        );

        // 유저 생성
        for (int i = 0; i < 20; i++) {

            UserEntity user = UserEntity.builder()
                    .userId("user" + i)
                    .userPw("1234")
                    .userType(UserType.CREATOR)
                    .build();

            userRepository.save(user);
        }

        int threadCount = 20;

        ExecutorService executorService =
                Executors.newFixedThreadPool(20);

        CountDownLatch latch =
                new CountDownLatch(threadCount);

        // when

        for (int i = 0; i < threadCount; i++) {

            int index = i;

            executorService.submit(() -> {

                try {
                    enrollService.createEnroll(
                            classEntity.getClassSeq(),
                            "user" + index
                    );

                } catch (Exception e) {
                    System.out.println(e.getMessage());

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then

        ClassEntity result = classRepository.findById(
                classEntity.getClassSeq()
        ).orElseThrow();

        Assertions.assertEquals(
                10,
                result.getClassCurrApps()
        );
    }

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

        // 강의 2개 생성
        ClassEntity class1 = classRepository.save(
                ClassEntity.builder()
                        .classTitle("강의1")
                        .classContent("내용1")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(30)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .classState(ClassState.OPEN)
                        .user(creator)
                        .build()
        );

        ClassEntity class2 = classRepository.save(
                ClassEntity.builder()
                        .classTitle("강의2")
                        .classContent("내용2")
                        .classPrice(BigDecimal.valueOf(2000))
                        .classMaxCap(30)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .classState(ClassState.OPEN)
                        .user(creator)
                        .build()
        );

        // 사용자 20명 생성 + 수강 신청
        for (int i = 1; i <= 20; i++) {

            UserEntity student = userRepository.save(
                    UserEntity.builder()
                            .userId("student" + i)
                            .userPw("1234")
                            .userType(UserType.CLASSMATE)
                            .build()
            );

            // 홀수 -> class1 신청
            if (i % 2 == 1) {

                enrollRepository.save(
                        EnrollEntity.builder()
                                .user(student)
                                .classEntity(class1)
                                .enrollState(EnrollState.CONFIRMED)
                                .build()
                );
            }

            // 짝수 -> class2 신청
            else {

                enrollRepository.save(
                        EnrollEntity.builder()
                                .user(student)
                                .classEntity(class2)
                                .enrollState(EnrollState.CONFIRMED)
                                .build()
                );
            }
        }

        // when

        Page<ResponseGetUserEnrollClass> result =
                enrollService.getClassEnrollUserList(
                        0,
                        10,
                        class1.getClassSeq(),
                        creator.getUserId()
                );

        // then

        System.out.println("===== class1 수강생 목록 =====");

        result.forEach(res -> {

            System.out.println(
                    "enrollSeq = " + res.getEnrollSeq()
            );

            System.out.println(
                    "userId = " + res.getUserId()
            );
        });

        // class1 은 홀수 사용자만 -> 10명
        Assertions.assertEquals(
                10,
                result.getTotalElements()
        );

        // 첫 사용자 확인
        Assertions.assertEquals(
                "student1",
                result.getContent().get(0).getUserId()
        );
    }

}
