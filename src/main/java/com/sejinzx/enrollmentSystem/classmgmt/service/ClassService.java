package com.sejinzx.enrollmentSystem.classmgmt.service;

import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestAddClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestUpdateClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.ResponseGetClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.ResponseGetDetailClass;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UserService userService;

    /**
     * 강의 등록
     */
    public Long createClass(RequestAddClass requestAddClass, String userId) {

        // 1. 사용자 Type 확인
        UserEntity user = userService.validateCreator(userId);

        // 2. 강의 등록
        ClassEntity classEntity = ClassEntity.builder()
                .classState(ClassState.DRAFT)
                .classEndDate(requestAddClass.getClassEndDate())
                .classContent(requestAddClass.getClassContent())
                .classTitle(requestAddClass.getClassTitle())
                .classPrice(requestAddClass.getClassPrice())
                .classMaxCap(requestAddClass.getClassMaxCap())
                .classStartDate(requestAddClass.getClassStartDate())
                .user(user)
                .build();

        return classRepository.save(classEntity).getClassSeq();
    }

    /**
     * 강의 수정
     */
    @Transactional
    public Long updateClass(Long classSeq, RequestUpdateClass requestUpdateClass, String userId) {

        // 1. 사용자 조회
        Long userSeq = userService.findActiveUser(userId).getUserSeq();

        // 2. 본인 강의 확인
        ClassEntity classEntity = getValidateMyClass(classSeq, userSeq);

        // 3. 모집 상태에 따른 수정 가능 여부 확인
        if(classEntity.getClassState() == ClassState.OPEN ||
                classEntity.getClassState() == ClassState.CLOSED) {
            throw new BusinessException(ErrorCode.CLASS_MODIFICATION_NOT_ALLOWED);
        }

        // 4. 강의 수정
        classEntity.updateClass(
                requestUpdateClass.getClassTitle(),
                requestUpdateClass.getClassContent(),
                requestUpdateClass.getClassPrice(),
                requestUpdateClass.getClassMaxCap(),
                requestUpdateClass.getClassStartDate(),
                requestUpdateClass.getClassEndDate()
        );

        return classEntity.getClassSeq();
    }

    /**
     * 강의 목록 조회
     */
    public Page<ResponseGetClass> getListClass(int page, int size, ClassState state) {

        // 1. 페이징 조건 설정
        Pageable pageable = PageRequest.of(page, size);

        Page<ClassEntity> result =
                (state == null)
                        // 2-1. 강의 상태 없을 경우 전체 조회
                        ? classRepository.findByClassDeletedFalse(pageable)
                        // 2-2. 강의 상태 별 조회
                        : classRepository.findByClassStateAndClassDeletedFalse(state, pageable);

        // 2. Entity -> DTO 변환
        return result.map(this::toResponseGetClass);
    }

    /**
     * 강의 상세 조회
     */
    public ResponseGetDetailClass getDetailClass(Long classSeq) {

        return toResponseGetDetailClass(getClass(classSeq));
    }

    /**
     * 강의 삭제 (soft delete)
     */
    @Transactional
    public Long deleteClass(Long classSeq, String userId) {

        // 1. 사용자 조회
        Long userSeq = userService.findActiveUser(userId).getUserSeq();

        // 2. 본인 강의 확인
        ClassEntity classEntity = getValidateMyClass(classSeq, userSeq);

        // 3. 모집 상태에 따른 삭제 가능 여부 확인
        if(classEntity.getClassState() == ClassState.OPEN ||
                classEntity.getClassState() == ClassState.CLOSED) {
            throw new BusinessException(ErrorCode.CLASS_DELETE_NOT_ALLOWED);
        }

        // 4. classDeleted: false -> true 변경
        classEntity.deleteClass();

        return classEntity.getClassSeq();
    }

    /**
     * 강의 정보 조회
     */
    public ClassEntity getClass(Long classSeq) {

        return classRepository.findByClassSeqAndClassDeletedFalse(classSeq)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASS_NOT_FOUND));
    }

    /**
     * 신청 가능 강의 조회 (비관적 락)
     */
    @Transactional
    public ClassEntity getAvailableClassWithLock(Long classSeq) {

        // 1. 강의 유무 확인
        ClassEntity classEntity = classRepository.findByIdWithLock(classSeq)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASS_NOT_FOUND));

        validateEnrollAvailable(classEntity);

        classEntity.increaseCurrApps();

        return classEntity;
    }

    /**
     * 신청 가능 여부 검증
     */
    private void validateEnrollAvailable(ClassEntity classEntity) {

        // 강의가 오픈되지 않은 경우
        if (classEntity.getClassState() != ClassState.OPEN) {
            throw new BusinessException(ErrorCode.CLASS_NOT_OPEN);
        }

        // 강의 정원을 초과한 경우
        if (classEntity.getClassCurrApps()
                >= classEntity.getClassMaxCap()) {
            throw new BusinessException(ErrorCode.CLASS_CAPACITY_FULL);
        }
    }

    @Transactional
    public void increaseCurrApps(Long classSeq) {

        ClassEntity classEntity =
                classRepository.findById(classSeq)
                        .orElseThrow(() ->
                                new BusinessException(ErrorCode.CLASS_NOT_FOUND));

        validateEnrollAvailable(classEntity);

        classEntity.increaseCurrApps();
    }

    /**
     * 클래스 상태 변경
     */
    @Transactional
    public void updateClassState() {

        LocalDate today = LocalDate.now();

        // 1. 모집 예정 -> 모집중 변경
        classRepository.findByClassStateAndClassStartDateLessThanEqual(ClassState.DRAFT, today)
                .forEach(ClassEntity::openClass);

        // 2. 모집중 -> 모집 종료 변경
        classRepository.findByClassStateAndClassEndDateBefore(ClassState.OPEN, today)
                .forEach(ClassEntity::closeClass);
    }

    /**
     * 본인 강의 확인
     */
    public ClassEntity getValidateMyClass(Long classSeq, Long userSeq) {

        return classRepository.findByClassSeqAndUser_UserSeqAndClassDeletedFalse(classSeq, userSeq)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERS_CLASS_NOT_FOUND));
    }

    /**
     * Entity -> DTO
     */
    private ResponseGetClass toResponseGetClass(ClassEntity classEntity) {

        return ResponseGetClass.builder()
                .classSeq(classEntity.getClassSeq())
                .classTitle(classEntity.getClassTitle())
                .classState(classEntity.getClassState())
                .classPrice(classEntity.getClassPrice())
                .classMaxCap(classEntity.getClassMaxCap())
                .build();
    }

    /**
     * Entity -> DTO
     */
    private ResponseGetDetailClass toResponseGetDetailClass(ClassEntity classEntity) {

        return ResponseGetDetailClass.builder()
                .classSeq(classEntity.getClassSeq())
                .classTitle(classEntity.getClassTitle())
                .classContent(classEntity.getClassContent())
                .classState(classEntity.getClassState())
                .classCurrApps(classEntity.getClassCurrApps())
                .classEndDate(classEntity.getClassEndDate())
                .classStartDate(classEntity.getClassStartDate())
                .classPrice(classEntity.getClassPrice())
                .classMaxCap(classEntity.getClassMaxCap())
                .build();
    }
}
