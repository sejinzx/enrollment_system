package com.sejinzx.enrollmentSystem.classmgmt.dto;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
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
    private ClassState classState;

}
