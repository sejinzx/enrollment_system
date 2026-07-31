package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.MySqlContainerTest;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.enroll.repository.EnrollRepository;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
class EnrollServiceConcurrencyTest extends MySqlContainerTest {

    private static final int CLASS_CAPACITY = 100;
    private static final int REQUEST_COUNT = 200;

    @Autowired
    private EnrollService enrollService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @Test
    @DisplayName("정원 100명인 강의에 200명이 동시에 신청해도 100명만 성공")
    void processEnroll_concurrentRequests_capacityMaintained() throws Exception {

        // given
        UserEntity creator = createUser(
                "concurrency-creator",
                UserType.CREATOR
        );

        ClassEntity classEntity = createClass(
                creator,
                CLASS_CAPACITY
        );

        List<UserEntity> students = createStudents(REQUEST_COUNT);

        ExecutorService executorService =
                Executors.newFixedThreadPool(REQUEST_COUNT);

        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<EnrollResult>> futures = new ArrayList<>();

        try {
            // when
            for (UserEntity student : students) {
                futures.add(
                        executorService.submit(() ->
                                requestEnroll(
                                        startLatch,
                                        classEntity.getClassSeq(),
                                        student.getUserId()
                                )
                        )
                );
            }

            startLatch.countDown();

            int successCount = 0;
            int capacityFullCount = 0;

            for (Future<EnrollResult> future : futures) {
                EnrollResult result = future.get();

                if (result.success()) {
                    successCount++;
                } else if (result.errorCode() == ErrorCode.CLASS_CAPACITY_FULL) {
                    capacityFullCount++;
                }
            }

            // then
            long enrollCount =
                    enrollRepository.countByClassEntity_ClassSeq(
                            classEntity.getClassSeq()
                    );

            ClassEntity resultClass = classRepository.findById(
                    classEntity.getClassSeq()
            ).orElseThrow();

            Assertions.assertEquals(CLASS_CAPACITY, successCount);
            Assertions.assertEquals(REQUEST_COUNT - CLASS_CAPACITY, capacityFullCount);
            Assertions.assertEquals(CLASS_CAPACITY, enrollCount);
            Assertions.assertEquals(CLASS_CAPACITY, resultClass.getClassCurrApps());

        } finally {
            executorService.shutdown();
            executorService.awaitTermination(
                    10,
                    TimeUnit.SECONDS
            );
        }
    }

    @Test
    @DisplayName("동일 사용자가 동시에 여러 번 신청해도 한 건만 등록")
    void processEnroll_sameUserConcurrent_duplicatePrevented() throws Exception {

        // given
        UserEntity creator = createUser(
                "duplicate-creator",
                UserType.CREATOR
        );

        UserEntity student = createUser(
                "duplicate-student",
                UserType.CLASSMATE
        );

        ClassEntity classEntity = createClass(creator, 10);

        int requestCount = 20;

        ExecutorService executorService =
                Executors.newFixedThreadPool(requestCount);

        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<EnrollResult>> futures = new ArrayList<>();

        try {
            // when
            for (int i = 0; i < requestCount; i++) {
                futures.add(
                        executorService.submit(() ->
                                requestEnroll(
                                        startLatch,
                                        classEntity.getClassSeq(),
                                        student.getUserId()
                                )
                        )
                );
            }

            startLatch.countDown();

            int successCount = 0;
            int duplicateCount = 0;

            for (Future<EnrollResult> future : futures) {
                EnrollResult result = future.get();

                if (result.success()) {
                    successCount++;
                } else if (result.errorCode() == ErrorCode.DUPLICATE_ENROLL) {
                    duplicateCount++;
                }
            }

            // then
            long enrollCount =
                    enrollRepository.countByClassEntity_ClassSeq(
                            classEntity.getClassSeq()
                    );

            Assertions.assertEquals(1, successCount);
            Assertions.assertEquals(requestCount - 1, duplicateCount);
            Assertions.assertEquals(1, enrollCount);

        } finally {
            executorService.shutdown();
        }
    }

    private record EnrollResult(
            boolean success,
            ErrorCode errorCode
    ) {
    }

    private EnrollResult requestEnroll(
            CountDownLatch startLatch,
            Long classSeq,
            String userId
    ) {
        try {
            startLatch.await();

            enrollService.processEnroll(classSeq, userId);

            return new EnrollResult(true, null);

        } catch (BusinessException exception) {
            return new EnrollResult(
                    false,
                    exception.getErrorCode()
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(exception);
        }
    }
    private List<UserEntity> createStudents(int count) {

        List<UserEntity> students = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            students.add(
                    createUser(
                            "concurrency-student-" + i,
                            UserType.CLASSMATE
                    )
            );
        }

        return students;
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
                        .classTitle("concurrency test class")
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
}