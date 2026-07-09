package com.sejinzx.enrollmentSystem.enroll.kafka;

import com.sejinzx.enrollmentSystem.enroll.dto.EnrollmentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRequest(EnrollmentRequestedEvent event) {

        event.setRequestTime(System.currentTimeMillis());

        kafkaTemplate.send(
                "enrollment-request",
                event.getClassSeq().toString(),
                event
        );
    }
}
