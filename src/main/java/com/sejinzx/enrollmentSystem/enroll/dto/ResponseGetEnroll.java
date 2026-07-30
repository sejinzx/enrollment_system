package com.sejinzx.enrollmentSystem.enroll.dto;

import com.sejinzx.enrollmentSystem.enroll.entity.EnrollState;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResponseGetEnroll {

    private Long enrollSeq;
    private EnrollState enrollState;
    private String classTitle;
    private LocalDateTime classStartDate;
    private LocalDateTime classEndDate;

    @Builder
    public ResponseGetEnroll(Long enrollSeq, EnrollState enrollState,
                             String classTitle, LocalDateTime classStartDate, LocalDateTime classEndDate) {
        this.enrollSeq = enrollSeq;
        this.enrollState = enrollState;
        this.classTitle = classTitle;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
    }
}
