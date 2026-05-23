package com.sejinzx.enrollmentSystem.classmgmt.dto;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class ResponseGetClass {

    private Long classSeq;
    private String classTitle;
    private BigDecimal classPrice;
    private int classMaxCap;
    private ClassState classState;

    @Builder
    public ResponseGetClass(Long classSeq, int classMaxCap, String classTitle,
                            BigDecimal classPrice, ClassState classState) {
        this.classSeq = classSeq;
        this.classTitle = classTitle;
        this.classPrice = classPrice;
        this.classMaxCap = classMaxCap;
        this.classState = classState;
    }
}
