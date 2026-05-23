package com.sejinzx.enrollmentSystem.scheduler;

import com.sejinzx.enrollmentSystem.classmgmt.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassScheduler {

    private final ClassService classService;

    /**
     * 매일 자정 강의 상태 자동 변경
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateClassState() {

        log.info("강의 상태 자동 변경 시작");

        classService.updateClassState();

        log.info("강의 상태 자동 변경 종료");
    }
}
