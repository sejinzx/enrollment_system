package com.sejinzx.enrollmentSystem.enroll.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ResponseGetUserEnrollClass {

    private Long enrollSeq;
    private String userId;

    @Builder
    public ResponseGetUserEnrollClass(Long enrollSeq, String userId) {
        this.enrollSeq = enrollSeq;
        this.userId = userId;
    }

}
