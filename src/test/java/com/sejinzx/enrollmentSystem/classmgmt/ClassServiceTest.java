package com.sejinzx.enrollmentSystem.classmgmt;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.classmgmt.service.ClassService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
public class ClassServiceTest {

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassRepository classRepository;

    @Test
    void updateClassState_test() {

        // given
        LocalDate today = LocalDate.now();

        // 모집 예정 강의 생성
        ClassEntity draftClass = classRepository.save(
                ClassEntity.builder()
                        .classTitle("draft")
                        .classContent("draft")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(today.minusDays(1))
                        .classEndDate(today.plusDays(5))
                        .classState(ClassState.DRAFT)
                        .build()
        );

        // 모집 종료 대상 강의 생성
        ClassEntity openClass = classRepository.save(
                ClassEntity.builder()
                        .classTitle("open")
                        .classContent("open")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(today.minusDays(10))
                        .classEndDate(today.minusDays(1))
                        .classState(ClassState.OPEN)
                        .build()
        );

        System.out.println("===== 상태 변경 전 =====");
        System.out.println(
                "draftClass state = " + draftClass.getClassState()
        );
        System.out.println(
                "openClass state = " + openClass.getClassState()
        );

        // when
        // 상태 자동 변경 실행
        classService.updateClassState();

        // then
        // 변경된 강의 재조회
        ClassEntity resultDraft =
                classRepository.findById(draftClass.getClassSeq())
                        .orElseThrow();

        ClassEntity resultOpen =
                classRepository.findById(openClass.getClassSeq())
                        .orElseThrow();

        System.out.println("===== 상태 변경 후 =====");
        System.out.println(
                "resultDraft state = " + resultDraft.getClassState()
        );
        System.out.println(
                "resultOpen state = " + resultOpen.getClassState()
        );

        // DRAFT -> OPEN 변경 확인
        Assertions.assertEquals(
                ClassState.OPEN,
                resultDraft.getClassState()
        );

        // OPEN -> CLOSED 변경 확인
        Assertions.assertEquals(
                ClassState.CLOSED,
                resultOpen.getClassState()
        );
    }
}
