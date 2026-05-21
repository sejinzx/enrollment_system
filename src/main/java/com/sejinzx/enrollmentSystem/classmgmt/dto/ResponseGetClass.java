package com.sejinzx.enrollmentSystem.classmgmt.dto;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class ResponseGetClass {

    private Long classSeq;
    private String classTitle;
    private String classContent;
    private BigDecimal classPrice;
    private int classMaxCap;
    private int classCurrApps;
    private LocalDate classStartDate;
    private LocalDate classEndDate;
    private ClassState classState;

    @Builder
    public ResponseGetClass(Long classSeq, int classCurrApps, int classMaxCap,
                            String classContent, String classTitle, BigDecimal classPrice,
                            ClassState classState, LocalDate classEndDate, LocalDate classStartDate) {
        this.classSeq = classSeq;
        this.classTitle = classTitle;
        this.classContent = classContent;
        this.classPrice = classPrice;
        this.classCurrApps = classCurrApps;
        this.classMaxCap = classMaxCap;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
        this.classState = classState;
    }
}
