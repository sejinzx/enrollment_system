package com.sejinzx.enrollmentSystem.classmgmt.service;

import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestAddClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestUpdateClass;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void before() {
        System.out.println("===== 테스트 시작 =====");
    }

    @AfterEach
    void after() {
        System.out.println("===== 테스트 종료 =====");
    }

    /**
     * 강의 등록 성공 테스트
     */
    @Test
    void createClass_success_test() {

        // given
        UserEntity creator = createCreator("creator1");

        RequestAddClass request = RequestAddClass.builder()
                .classTitle("Spring")
                .classContent("Backend")
                .classPrice(BigDecimal.valueOf(10000))
                .classMaxCap(20)
                .classStartDate(LocalDate.now().plusDays(1))
                .classEndDate(LocalDate.now().plusDays(10))
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

        System.out.println("강의 등록 성공");
    }

    /**
     * 강의 수정 성공 테스트
     */
    @Test
    void updateClass_success_test() {

        // given
        UserEntity creator = createCreator("creator2");

        ClassEntity classEntity =
                createClassEntity(
                        creator,
                        ClassState.DRAFT
                );

        RequestUpdateClass request =
                RequestUpdateClass.builder()
                        .classTitle("new title")
                        .classContent("new content")
                        .classPrice(BigDecimal.valueOf(5000))
                        .classMaxCap(30)
                        .classStartDate(LocalDate.now().plusDays(2))
                        .classEndDate(LocalDate.now().plusDays(20))
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
                "new title",
                result.getClassTitle()
        );

        Assertions.assertEquals(
                0,
                BigDecimal.valueOf(5000)
                        .compareTo(result.getClassPrice())
        );

        System.out.println("강의 수정 성공");
    }

    /**
     * 모집중 강의 수정 실패 테스트
     */
    @Test
    void updateClass_open_fail_test() {

        // given
        UserEntity creator = createCreator("creator3");

        ClassEntity classEntity =
                createClassEntity(
                        creator,
                        ClassState.OPEN
                );

        RequestUpdateClass request =
                RequestUpdateClass.builder()
                        .classTitle("update")
                        .classContent("update")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .build();

        // when
        BusinessException exception =
                Assertions.assertThrows(
                        BusinessException.class,
                        () -> classService.updateClass(
                                classEntity.getClassSeq(),
                                request,
                                creator.getUserId()
                        )
                );

        // then
        Assertions.assertEquals(
                ErrorCode.CLASS_MODIFICATION_NOT_ALLOWED,
                exception.getErrorCode()
        );

        System.out.println("모집중 강의 수정 실패");
    }

    /**
     * 강의 삭제 성공 테스트
     */
    @Test
    void deleteClass_success_test() {

        // given
        UserEntity creator = createCreator("creator4");

        ClassEntity classEntity =
                createClassEntity(
                        creator,
                        ClassState.DRAFT
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

        System.out.println("강의 삭제 성공");
    }

    /**
     * 클래스 상태 변경 테스트
     */
    @Test
    void updateClassState_test() {

        // given
        UserEntity creator = createCreator("creator5");

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

        System.out.println("모집 상태 변경 성공");
    }

    /**
     * creator 생성
     */
    private UserEntity createCreator(String userId) {

        return userRepository.save(
                UserEntity.builder()
                        .userId(userId)
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );
    }

    /**
     * class 생성
     */
    private ClassEntity createClassEntity(
            UserEntity creator,
            ClassState state
    ) {

        return classRepository.save(
                ClassEntity.builder()
                        .classTitle("title")
                        .classContent("content")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .classState(state)
                        .user(creator)
                        .build()
        );
    }
}