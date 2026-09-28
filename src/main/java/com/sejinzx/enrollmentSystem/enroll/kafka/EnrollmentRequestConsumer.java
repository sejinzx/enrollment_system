package com.sejinzx.enrollmentSystem.enroll.kafka;

import com.sejinzx.enrollmentSystem.enroll.dto.EnrollmentRequestedEvent;
import com.sejinzx.enrollmentSystem.enroll.service.EnrollService;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentRequestConsumer {

    private final EnrollService enrollService;

    @KafkaListener(
            topics = "enrollment-request",
            groupId = "enrollment-group",
            concurrency = "3"
    )
    public void consume(EnrollmentRequestedEvent req) {

        try {

            enrollService.processEnroll(
                    req.getClassSeq(),
                    req.getUserId()
            );

            long elapsed =
                    System.currentTimeMillis()
                            - req.getRequestTime();

            log.info(
                    "ENROLL_SUCCESS EndToEnd={}ms, classSeq={}, userId={}",
                    elapsed,
                    req.getClassSeq(),
                    req.getUserId()
            );

        } catch (BusinessException e) {

            log.info(
                    "ENROLL_FAIL reason={}, classSeq={}, userId={}",
                    e.getMessage(),
                    req.getClassSeq(),
                    req.getUserId()
            );
        } catch (Exception e) {

            log.error(
                    "ENROLL_SYSTEM_ERROR classSeq={}, userId={}",
                    req.getClassSeq(),
                    req.getUserId(),
                    e
            );

            throw e;
        }
    }
}