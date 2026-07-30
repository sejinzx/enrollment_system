package com.sejinzx.enrollmentSystem.classmgmt.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class RequestUpdateClass {

    private String classTitle;
    private String classContent;
    private BigDecimal classPrice;
    private int classMaxCap;
    private LocalDateTime classStartDate;
    private LocalDateTime classEndDate;

    @Builder
    public RequestUpdateClass(String classTitle, String classContent,
                              BigDecimal classPrice, int classMaxCap,
                              LocalDateTime classStartDate, LocalDateTime classEndDate) {
        this.classTitle = classTitle;
        this.classContent = classContent;
        this.classPrice = classPrice;
        this.classMaxCap = classMaxCap;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
    }
}
