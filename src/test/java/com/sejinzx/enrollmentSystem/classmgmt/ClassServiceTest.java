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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
class ClassServiceTest {

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    /*
     * 강의 등록 테스트
     * 예상 결과: 강의 등록 성공
     */
    @Test
    void createClass_success_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        RequestAddClass request = RequestAddClass.builder()
                .classTitle("Spring Boot")
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
                "Spring Boot",
                result.getClassTitle()
        );

        Assertions.assertEquals(
                ClassState.DRAFT,
                result.getClassState()
        );

        System.out.println("강의 등록 성공");
    }

    /*
     * 강의 수정 테스트
     * 예상 결과: 수정 성공
     */
    @Test
    void updateClass_success_test() {

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
                        .classStartDate(LocalDate.now().plusDays(1))
                        .classEndDate(LocalDate.now().plusDays(10))
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
                        .classEndDate(LocalDate.now().plusDays(15))
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

        System.out.println("강의 수정 성공");
    }

    /*
     * 강의 수정 실패 테스트
     * 예상 결과: 본인 글 아님
     */
    @Test
    void updateClass_fail_test() {

        // given
        UserEntity creator = userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        UserEntity otherUser = userRepository.save(
                UserEntity.builder()
                        .userId("other")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        ClassEntity classEntity = classRepository.save(
                ClassEntity.builder()
                        .classTitle("class")
                        .classContent("content")
                        .classPrice(BigDecimal.valueOf(1000))
                        .classMaxCap(10)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(5))
                        .classState(ClassState.DRAFT)
                        .user(creator)
                        .build()
        );

        RequestUpdateClass request =
                RequestUpdateClass.builder()
                        .classTitle("hack")
                        .classContent("hack")
                        .classPrice(BigDecimal.valueOf(9999))
                        .classMaxCap(99)
                        .classStartDate(LocalDate.now())
                        .classEndDate(LocalDate.now().plusDays(20))
                        .build();

        // when
        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> classService.updateClass(
                                classEntity.getClassSeq(),
                                request,
                                otherUser.getUserId()
                        )
                );

        // then
        Assertions.assertEquals("User's Class not found", exception.getMessage());

        System.out.println("본인 글 아님 예외 발생");
    }

    /*
     * 강의 삭제 테스트
     * 예상 결과: 삭제 성공
     */
    @Test
    void deleteClass_success_test() {

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
                        .classEndDate(LocalDate.now().plusDays(5))
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

        System.out.println("강의 삭제 성공");
    }

    /*
     * 모집 상태 변경 테스트
     * 예상 결과:
     * DRAFT -> OPEN
     * OPEN -> CLOSED
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

        System.out.println("모집 상태 변경 성공");
    }
}