package com.sejinzx.enrollmentSystem.classmgmt.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class RequestAddClass {

    private String classTitle;
    private String classContent;
    private BigDecimal classPrice;
    private int classMaxCap;
    private LocalDate classStartDate;
    private LocalDate classEndDate;

    @Builder
    public RequestAddClass(String classTitle, String classContent,
                           BigDecimal classPrice, int classMaxCap,
                           LocalDate classStartDate, LocalDate classEndDate) {
        this.classTitle = classTitle;
        this.classContent = classContent;
        this.classPrice = classPrice;
        this.classMaxCap = classMaxCap;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
    }
}
