package com.sejinzx.enrollmentSystem.classmgmt.dto;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class RequestUpdateClass {

    private String classTitle;
    private String classContent;
    private BigDecimal classPrice;
    private int classMaxCap;
    private LocalDate classStartDate;
    private LocalDate classEndDate;
    private ClassState classState;

    @Builder
    public RequestUpdateClass(String classTitle, String classContent,
                              BigDecimal classPrice, int classMaxCap,
                              LocalDate classStartDate, LocalDate classEndDate, ClassState classState) {
        this.classTitle = classTitle;
        this.classContent = classContent;
        this.classPrice = classPrice;
        this.classMaxCap = classMaxCap;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
        this.classState = classState;
    }
}
