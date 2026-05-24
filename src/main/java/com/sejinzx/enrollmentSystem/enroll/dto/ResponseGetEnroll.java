package com.sejinzx.enrollmentSystem.enroll.dto;

import com.sejinzx.enrollmentSystem.enroll.entity.EnrollState;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ResponseGetEnroll {

    private Long enrollSeq;
    private EnrollState enrollState;
    private String classTitle;
    private LocalDate classStartDate;
    private LocalDate classEndDate;

    @Builder
    public ResponseGetEnroll(Long enrollSeq, EnrollState enrollState,
                             String classTitle, LocalDate classStartDate, LocalDate classEndDate) {
        this.enrollSeq = enrollSeq;
        this.enrollState = enrollState;
        this.classTitle = classTitle;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
    }
}
