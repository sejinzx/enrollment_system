package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
