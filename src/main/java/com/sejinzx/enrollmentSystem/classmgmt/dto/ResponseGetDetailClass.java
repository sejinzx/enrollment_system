package com.sejinzx.enrollmentSystem.classmgmt.dto;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ResponseGetDetailClass {

    private Long classSeq;
    private String classTitle;
    private String classContent;
    private BigDecimal classPrice;
    private int classMaxCap;
    private int classCurrApps;
    private LocalDateTime classStartDate;
    private LocalDateTime classEndDate;
    private ClassState classState;


    @Builder

    public ResponseGetDetailClass(Long classSeq, String classTitle, String classContent,
                                  BigDecimal classPrice, int classMaxCap, int classCurrApps,
                                  LocalDateTime classStartDate, LocalDateTime classEndDate, ClassState classState) {
        this.classSeq = classSeq;
        this.classTitle = classTitle;
        this.classContent = classContent;
        this.classPrice = classPrice;
        this.classMaxCap = classMaxCap;
        this.classCurrApps = classCurrApps;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
        this.classState = classState;
    }
}
