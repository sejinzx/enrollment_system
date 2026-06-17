package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
@ActiveProfiles("test")
class EnrollServiceConcurrencyTest {

    @Autowired
    private EnrollService enrollService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @Test
    void concurrentEnrollTest() throws Exception {

        long startTime = System.currentTimeMillis();

        // given
        UserEntity creator =
                createCreator("creator");

        ClassEntity classEntity =
                createClassEntity(creator, 100);

        int threadCount = 200;

        ExecutorService executorService =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<String>> futures =
                new ArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {

            UserEntity student =
                    createStudent("student" + i);

            int idx = i + 1;

            futures.add(
                    executorService.submit(() -> {

                        try {
                            startLatch.await();

                            enrollService.createEnroll(
                                    classEntity.getClassSeq(),
                                    student.getUserId()
                            );

                            return "SUCCESS - idx: " + idx;

                        } catch (Exception e) {

                            return "FAIL - idx: " + idx +
                                    ", reason: " +
                                    e.getMessage();
                        }
                    })
            );
        }

        startLatch.countDown();

        System.out.println("===== 결과 =====");

        for (Future<String> future : futures) {
            System.out.println(future.get());
        }

        executorService.shutdown();
        executorService.awaitTermination(
                5,
                TimeUnit.SECONDS
        );

        // then
        long enrollCount =
                enrollRepository.count();

        System.out.println(
                "최종 신청 인원 : " + enrollCount
        );

        long endTime = System.currentTimeMillis();

        System.out.println(
                "총 실행 시간 : " +
                        (endTime - startTime) + "ms"
        );

        Assertions.assertEquals(
                100,
                enrollCount
        );
    }

    private UserEntity createCreator(String userId) {

        return userRepository.save(
                UserEntity.builder()
                        .userId(userId)
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );
    }

    private UserEntity createStudent(String userId) {

        return userRepository.save(
                UserEntity.builder()
                        .userId(userId)
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );
    }

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
