package com.sejinzx.enrollmentSystem.classmgmt;

import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestAddClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestUpdateClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.ResponseGetClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.ResponseGetDetailClass;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.classmgmt.service.ClassService;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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

    @Autowired
    private UserRepository userRepository;

    /*
     * 강의 등록 테스트
     */
    @Test
    void createClass_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        RequestAddClass request = RequestAddClass.builder()
                .classTitle("Spring")
                .classContent("Backend")
                .classPrice(BigDecimal.valueOf(10000))
                .classMaxCap(20)
                .classStartDate(LocalDate.now().plusDays(5))
                .classEndDate(LocalDate.now().plusDays(15))
                .build();

        // when
        Long classSeq =
                classService.createClass(
                        request,
                        creator.getUserId()
                );

        // then
        ClassEntity result =
                classRepository.findById(classSeq)
                        .orElseThrow();

        Assertions.assertEquals(
                "Spring",
                result.getClassTitle()
        );

        Assertions.assertEquals(
                ClassState.DRAFT,
                result.getClassState()
        );
    }

    /*
     * 강의 수정 테스트
     */
    @Test
    void updateClass_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("old")
                        .classContent("old")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(LocalDate.now().plusDays(5))
                        .classEndDate(LocalDate.now().plusDays(15))
                        .classState(ClassState.DRAFT)
                        .user(creator)
                        .build()
        );

        RequestUpdateClass request =
                RequestUpdateClass.builder()
                        .classTitle("new")
                        .classContent("new content")
                        .classPrice(BigDecimal.valueOf(2000))
                        .classMaxCap(30)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(13))
                        .classState(ClassState.OPEN)
                        .build();

        // when
        classService.updateClass(
                classEntity.getClassSeq(),
                request,
                creator.getUserId()
        );

        // then
        ClassEntity result =
                classRepository.findById(
                        classEntity.getClassSeq()
                ).orElseThrow();

        Assertions.assertEquals(
                "new",
                result.getClassTitle()
        );

        Assertions.assertEquals(
                ClassState.OPEN,
                result.getClassState()
        );
    }

    /*
     * 강의 목록 조회 테스트
     */
    @Test
    void getListClass_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        for (int i = 1; i <= 10; i++) {

            classRepository.save(
                    ClassEntity.builder()
                            .classTitle("class" + i)
                            .classContent("content")
                            .classPrice(BigDecimal.valueOf(1000))
                            .classMaxCap(20)
                            .classStartDate(LocalDate.now())
                            .classEndDate(LocalDate.now().plusDays(5))
                            .classState(ClassState.OPEN)
                            .user(creator)
                            .build()
            );
        }

        for (int i = 11; i <= 20; i++) {

            classRepository.save(
                    ClassEntity.builder()
                            .classTitle("class" + i)
                            .classContent("content")
                            .classPrice(BigDecimal.valueOf(1000))
                            .classMaxCap(20)
                            .classStartDate(LocalDate.now().plusDays(5))
                            .classEndDate(LocalDate.now().plusDays(15))
                            .classState(ClassState.DRAFT)
                            .user(creator)
                            .build()
            );
        }

        // when
        Page<ResponseGetClass> result =
                classService.getListClass(
                        0,
                        10,
                        ClassState.OPEN
                );

        // then
        Assertions.assertEquals(
                20,
                result.getTotalElements()
        );

        Assertions.assertEquals(
                10,
                result.getContent().size()
        );
    }

    /*
     * 강의 상세 조회 테스트
     */
    @Test
    void getDetailClass_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("detail")
                        .classContent("detail content")
                        .classPrice(BigDecimal.valueOf(5000))
                        .classMaxCap(15)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(3))
                        .classState(ClassState.OPEN)
                        .user(creator)
                        .build()
        );

        // when
        ResponseGetDetailClass result =
                classService.getDetailClass(
                        classEntity.getClassSeq()
                );

        // then
        Assertions.assertEquals(
                "detail",
                result.getClassTitle()
        );

        Assertions.assertEquals(
                15,
                result.getClassMaxCap()
        );
    }

    /*
     * 강의 삭제 테스트
     */
    @Test
    void deleteClass_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("delete")
                        .classContent("delete")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(1))
                        .classState(ClassState.OPEN)
                        .user(creator)
                        .build()
        );

        // when
        classService.deleteClass(
                classEntity.getClassSeq(),
                creator.getUserId()
        );

        // then
        ClassEntity result =
                classRepository.findById(
                        classEntity.getClassSeq()
                ).orElseThrow();

        Assertions.assertTrue(
                result.isClassDeleted()
        );
    }

    /*
     * 클래스 상태 변경 테스트
     */
    @Test
    void updateClassState_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        LocalDate today = LocalDate.now();

        ClassEntity draftClass = classRepository.save(
                ClassEntity.builder()
                        .classTitle("draft")
                        .classContent("draft")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(today.minusDays(1))
                        .classEndDate(today.plusDays(5))
                        .classState(ClassState.DRAFT)
                        .user(creator)
                        .build()
        );

        ClassEntity openClass = classRepository.save(
                ClassEntity.builder()
                        .classTitle("open")
                        .classContent("open")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(today.minusDays(10))
                        .classEndDate(today.minusDays(1))
                        .classState(ClassState.OPEN)
                        .user(creator)
                        .build()
        );

        // when
        classService.updateClassState();

        // then
        ClassEntity resultDraft =
                classRepository.findById(
                        draftClass.getClassSeq()
                ).orElseThrow();

        ClassEntity resultOpen =
                classRepository.findById(
                        openClass.getClassSeq()
                ).orElseThrow();

        Assertions.assertEquals(
                ClassState.OPEN,
                resultDraft.getClassState()
        );

        Assertions.assertEquals(
                ClassState.CLOSED,
                resultOpen.getClassState()
        );
    }
}
