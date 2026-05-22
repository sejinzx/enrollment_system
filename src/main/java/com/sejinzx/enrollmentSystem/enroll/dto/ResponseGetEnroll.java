package com.sejinzx.enrollmentSystem.enroll.dto;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollState;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ResponseGetEnroll {

    private Long enrollSeq;
    private EnrollState enrollState;
    private ClassEntity classEntity;

    @Builder
    public ResponseGetEnroll(Long enrollSeq, EnrollState enrollState, ClassEntity classEntity) {
        this.enrollSeq = enrollSeq;
        this.enrollState = enrollState;
        this.classEntity = classEntity;
    }

}
