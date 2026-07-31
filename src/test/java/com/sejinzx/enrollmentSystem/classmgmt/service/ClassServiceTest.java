package com.sejinzx.enrollmentSystem.classmgmt.service;

import com.sejinzx.enrollmentSystem.MySqlContainerTest;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClassServiceTest extends MySqlContainerTest {

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("강의 등록 성공")
    void createClass_success() {

        // given
        UserEntity creator = createCreator("creator1");

        RequestAddClass request = RequestAddClass.builder()
                .classTitle("Spring")
                .classContent("Backend")
                .classPrice(BigDecimal.valueOf(10000))
                .classMaxCap(20)
                .classStartDate(LocalDateTime.now().plusDays(1))
                .classEndDate(LocalDateTime.now().plusDays(10))
                .build();

        // when
        Long classSeq = classService.createClass(
                request,
                creator.getUserId()
        );

        // then
        ClassEntity result = classRepository.findById(classSeq)
                .orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        "Spring",
                        result.getClassTitle()
                ),
                () -> Assertions.assertEquals(
                        "Backend",
                        result.getClassContent()
                ),
                () -> Assertions.assertEquals(
                        ClassState.DRAFT,
                        result.getClassState()
                ),
                () -> Assertions.assertEquals(
                        0,
                        BigDecimal.valueOf(10000)
                                .compareTo(result.getClassPrice())
                ),
                () -> Assertions.assertEquals(
                        creator.getUserSeq(),
                        result.getUser().getUserSeq()
                )
        );
    }

    @Test
    @DisplayName("강의 수정 성공")
    void updateClass_success() {

        // given
        UserEntity creator = createCreator("creator2");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.DRAFT
        );

        RequestUpdateClass request = createUpdateRequest();

        // when
        Long resultClassSeq = classService.updateClass(
                classEntity.getClassSeq(),
                request,
                creator.getUserId()
        );

        // then
        ClassEntity result = classRepository.findById(
                classEntity.getClassSeq()
        ).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        classEntity.getClassSeq(),
                        resultClassSeq
                ),
                () -> Assertions.assertEquals(
                        "new title",
                        result.getClassTitle()
                ),
                () -> Assertions.assertEquals(
                        "new content",
                        result.getClassContent()
                ),
                () -> Assertions.assertEquals(
                        0,
                        BigDecimal.valueOf(5000)
                                .compareTo(result.getClassPrice())
                ),
                () -> Assertions.assertEquals(
                        30,
                        result.getClassMaxCap()
                )
        );
    }

    @Test
    @DisplayName("모집 중인 강의 수정 시 실패")
    void updateClass_open_fail() {

        // given
        UserEntity creator = createCreator("creator3");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.OPEN
        );

        RequestUpdateClass request = createUpdateRequest();

        // when
        BusinessException exception = Assertions.assertThrows(
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
    }

    @Test
    @DisplayName("모집 종료된 강의 수정 시 실패")
    void updateClass_closed_fail() {

        // given
        UserEntity creator = createCreator("creator4");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.CLOSED
        );

        RequestUpdateClass request = createUpdateRequest();

        // when
        BusinessException exception = Assertions.assertThrows(
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
    }

    @Test
    @DisplayName("다른 사용자의 강의 수정 불가")
    void updateClass_otherUser_fail() {

        // given
        UserEntity creator = createCreator("creator5");
        UserEntity otherCreator = createCreator("creator6");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.DRAFT
        );

        RequestUpdateClass request = createUpdateRequest();

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> classService.updateClass(
                        classEntity.getClassSeq(),
                        request,
                        otherCreator.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.USERS_CLASS_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("강의 삭제 성공")
    void deleteClass_success() {

        // given
        UserEntity creator = createCreator("creator7");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.DRAFT
        );

        // when
        Long resultClassSeq = classService.deleteClass(
                classEntity.getClassSeq(),
                creator.getUserId()
        );

        // then
        ClassEntity result = classRepository.findById(
                classEntity.getClassSeq()
        ).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        classEntity.getClassSeq(),
                        resultClassSeq
                ),
                () -> Assertions.assertTrue(
                        result.isClassDeleted()
                )
        );
    }

    @Test
    @DisplayName("모집 중인 강의 삭제 시 실패")
    void deleteClass_open_fail() {

        // given
        UserEntity creator = createCreator("creator8");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.OPEN
        );

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> classService.deleteClass(
                        classEntity.getClassSeq(),
                        creator.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.CLASS_DELETE_NOT_ALLOWED,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("모집 종료된 강의 삭제 시 실패")
    void deleteClass_closed_fail() {

        // given
        UserEntity creator = createCreator("creator9");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.CLOSED
        );

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> classService.deleteClass(
                        classEntity.getClassSeq(),
                        creator.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.CLASS_DELETE_NOT_ALLOWED,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("다른 사용자의 강의 삭제 불가")
    void deleteClass_otherUser_fail() {

        // given
        UserEntity creator = createCreator("creator10");
        UserEntity otherCreator = createCreator("creator11");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.DRAFT
        );

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> classService.deleteClass(
                        classEntity.getClassSeq(),
                        otherCreator.getUserId()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.USERS_CLASS_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("존재하지 않는 강의 조회 시 실패")
    void getClass_notFound_fail() {

        // given
        Long notExistClassSeq = 999999L;

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> classService.getClass(notExistClassSeq)
        );

        // then
        Assertions.assertEquals(
                ErrorCode.CLASS_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("삭제된 강의 조회 시 실패")
    void getClass_deleted_fail() {

        // given
        UserEntity creator = createCreator("creator12");

        ClassEntity classEntity = createClassEntity(
                creator,
                ClassState.DRAFT
        );

        classEntity.deleteClass();
        classRepository.flush();

        // when
        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> classService.getClass(
                        classEntity.getClassSeq()
                )
        );

        // then
        Assertions.assertEquals(
                ErrorCode.CLASS_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("클래스 상태 변경 성공")
    void updateClassState_success() {

        // given
        UserEntity creator = createCreator("creator13");

        LocalDateTime today = LocalDateTime.now();

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
        ClassEntity resultDraft = classRepository.findById(
                draftClass.getClassSeq()
        ).orElseThrow();

        ClassEntity resultOpen = classRepository.findById(
                openClass.getClassSeq()
        ).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        ClassState.OPEN,
                        resultDraft.getClassState()
                ),
                () -> Assertions.assertEquals(
                        ClassState.CLOSED,
                        resultOpen.getClassState()
                )
        );
    }

    /**
     * 수정 요청 생성
     */
    private RequestUpdateClass createUpdateRequest() {

        return RequestUpdateClass.builder()
                .classTitle("new title")
                .classContent("new content")
                .classPrice(BigDecimal.valueOf(5000))
                .classMaxCap(30)
                .classStartDate(LocalDateTime.now().plusDays(2))
                .classEndDate(LocalDateTime.now().plusDays(20))
                .build();
    }

    /**
     * 강사 생성
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
     * 강의 생성
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
                        .classStartDate(LocalDateTime.now())
                        .classEndDate(LocalDateTime.now().plusDays(5))
                        .classState(state)
                        .user(creator)
                        .build()
        );
    }
}