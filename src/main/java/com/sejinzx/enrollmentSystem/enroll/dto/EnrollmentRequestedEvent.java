package com.sejinzx.enrollmentSystem.enroll.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestedEvent {

    private String userId;
    private Long classSeq;
    private Long requestTime;

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }
}
