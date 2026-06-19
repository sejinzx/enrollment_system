package com.sejinzx.enrollmentSystem.enroll.kafka;

import com.sejinzx.enrollmentSystem.classmgmt.service.ClassService;
import com.sejinzx.enrollmentSystem.enroll.dto.EnrollmentRequestedEvent;
import com.sejinzx.enrollmentSystem.enroll.service.EnrollService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentRequestConsumer {

    private final EnrollService enrollService;
    private final ClassService classService;

    @KafkaListener(topics = "enrollment-request", groupId = "enrollment-group")
    public void consume(EnrollmentRequestedEvent req) {

        classService.increaseCurrApps(req.getClassSeq());
        enrollService.processEnroll(req.getClassSeq(), req.getUserId());
    }
}