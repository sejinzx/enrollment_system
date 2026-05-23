package com.sejinzx.enrollmentSystem.classmgmt.service;

import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestAddClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestUpdateClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.ResponseGetClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.ResponseGetDetailClass;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

        // 3. 저장
        ClassEntity saved = classRepository.save(classEntity);

        return saved.getClassSeq();
    }

    /**
     * 강의 수정
     */
    public Long updateClass(Long classSeq,
                                   RequestUpdateClass requestUpdateClass,
                                   String userId) {

        // 1. 강의 유무 확인
        ClassEntity classEntity = getClass(classSeq);

        // 2. 본인 글인지 확인
        if (!classEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인 글만 수정 가능");
        }

        // 3. 강의 수정
        classEntity.updateClass(
                requestUpdateClass.getClassTitle(),
                requestUpdateClass.getClassContent(),
                requestUpdateClass.getClassPrice(),
                requestUpdateClass.getClassMaxCap(),
                requestUpdateClass.getClassStartDate(),
                requestUpdateClass.getClassEndDate(),
                requestUpdateClass.getClassState()
        );

        // 4. 저장
        ClassEntity saved = classRepository.save(classEntity);

        return saved.getClassSeq();
    }

    /**
     * 강의 목록 조회
     */
    public Page<ResponseGetClass> getListClass(int page, int size, ClassState state) {

        // 1. 페이징 조건 설정
        Pageable pageable = PageRequest.of(page, size);

        Page<ClassEntity> result;

        // 2-1. 강의 상태 없을 경우 전체 조회
        if (state == null) {
            result = classRepository.findByClassDeletedFalse(pageable);
        }
        // 2-2. 강의 상태 별 조회
        else {
            result = classRepository.findByClassStateAndClassDeletedFalse(state, pageable);
        }

        // 3. Entity -> DTO 변환
        return result.map(classEntity -> ResponseGetClass.builder()
                .classSeq(classEntity.getClassSeq())
                .classTitle(classEntity.getClassTitle())
                .classState(classEntity.getClassState())
                .classPrice(classEntity.getClassPrice())
                .classMaxCap(classEntity.getClassMaxCap())
                .build());

    }

    /**
     * 강의 상세 조회
     */
    public ResponseGetDetailClass getDetailClass(Long classSeq) {

        // 1. 강의 유무 확인
        ClassEntity classEntity = getClass(classSeq);

        // 2. Entity -> DTO 변환
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

    /**
     * 강의 삭제 (soft delete)
     */
    public Long deleteClass(Long classSeq, String userId) {

        // 1. 강의 유무 확인
        ClassEntity classEntity = getClass(classSeq);

        // 2. 본인 글인지 확인
        if (!classEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인 글만 삭제 가능");
        }

        // 3. classDeleted: false -> true 변경
        classEntity.deleteClass();

        // 4. 저장
        ClassEntity saved = classRepository.save(classEntity);

        return saved.getClassSeq();

    }

    /**
     * 강의 정보 조회
     */
    public ClassEntity getClass(Long classSeq) {
        return classRepository.findByClassSeqAndClassDeletedFalse(classSeq)
                .orElseThrow(() -> new RuntimeException("Class not found"));
    }

    /**
     * 강의 신청 가능 여부 확인
     */
    @Transactional
    public ClassEntity validateCapacity(Long classSeq) {

        // 1. 강의 유무 확인
        ClassEntity classEntity = classRepository.findByIdWithLock(classSeq)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // 2. 강의 신청 가능 상태 확인
        if (classEntity.getClassState() != ClassState.OPEN) {
            throw new RuntimeException("신청 불가 상태");
        }

        // 3. 정원 초과 여부 확인
        if (classEntity.getClassCurrApps() >= classEntity.getClassMaxCap()) {
            throw new RuntimeException("정원 초과");
        }

        // 4. 현재 신청 인원 증가
        classEntity.increaseCurrApps();

        return classEntity;

    }

    /**
     *
     */
    @Transactional
    public void updateClassState() {

        LocalDate today = LocalDate.now();

        // 1. 모집 예정 -> 모집중 변경
        List<ClassEntity> openClasses =
                classRepository.findByClassStateAndClassStartDateLessThanEqual(
                        ClassState.DRAFT,
                        today
                );

        openClasses.forEach(ClassEntity::openClass);

        // 2. 모집중 -> 모집 종료 변경
        List<ClassEntity> closedClasses =
                classRepository.findByClassStateAndClassEndDateBefore(
                        ClassState.OPEN,
                        today
                );

        closedClasses.forEach(ClassEntity::closeClass);
    }

}
