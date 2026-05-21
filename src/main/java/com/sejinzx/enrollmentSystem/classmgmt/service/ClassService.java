package com.sejinzx.enrollmentSystem.classmgmt.service;

import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestAddClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestUpdateClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.ResponseGetClass;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.repository.ClassRepository;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    /**
     * 강의 등록
     */
    public ClassEntity createClass(RequestAddClass requestAddClass, String userId) {

        // 1. 사용자 Type 확인
        UserEntity user = validateCreator(userId);

        // 2. 강의 등록
        ClassEntity classEntity = ClassEntity.builder()
                .classState(requestAddClass.getClassState())
                .classEndDate(requestAddClass.getClassEndDate())
                .classContent(requestAddClass.getClassContent())
                .classTitle(requestAddClass.getClassTitle())
                .classPrice(requestAddClass.getClassPrice())
                .classMaxCap(requestAddClass.getClassMaxCap())
                .classStartDate(requestAddClass.getClassStartDate())
                .user(user)
                .build();

        return classRepository.save(classEntity);
    }

    /**
     * User Type 확인
     */
    public UserEntity validateCreator(String userId) {

        // 1. 사용자 유무 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. 사용자 Type 확인
        if (user.getUserType() != UserType.CREATOR) {
            throw new RuntimeException("권한 없음");
        }

        return user;
    }

    /**
     * 강의 수정
     */
    public ClassEntity updateClass(Long classSeq,
                                   RequestUpdateClass requestUpdateClass,
                                   String userId) {

        // 1. 강의 유무 확인
        ClassEntity classEntity = classRepository.findByClassSeqAndClassDeletedFalse(classSeq)
                .orElseThrow(() -> new RuntimeException("Class not found"));

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

        return classRepository.save(classEntity);
    }

    /**
     * 강의 목록 조회
     */
    public Page<ResponseGetClass> getListClass(int page, int size, ClassState state) {

        // 1. 페이징 조건 설정
        Pageable pageable = PageRequest.of(page, size);

        Page<ClassEntity> result;

        // 2-1. 강의 상태 없을 경우
        if (state == null) {
            result = classRepository.findByClassDeletedFalse(pageable);
        }
        // 2-2. 겅의 상태 별 조회
        else {
            result = classRepository.findByClassStateAndClassDeletedFalse(state, pageable);
        }

        // 3. Entity -> DTO 변환
        return result.map(classEntity -> ResponseGetClass.builder()
                .classSeq(classEntity.getClassSeq())
                .classTitle(classEntity.getClassTitle())
                .classContent(classEntity.getClassContent())
                .classState(classEntity.getClassState())
                .classCurrApps(classEntity.getClassCurrApps())
                .classEndDate(classEntity.getClassEndDate())
                .classStartDate(classEntity.getClassStartDate())
                .classPrice(classEntity.getClassPrice())
                .classMaxCap(classEntity.getClassMaxCap())
                .build());

    }

    /**
     * 강의 상세 조회
     */
    public ResponseGetClass getDetailClass(Long classSeq) {

        // 1. Class 존재 여부 확인
        ClassEntity result = classRepository.findByClassSeqAndClassDeletedFalse(classSeq)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // 2. Entity -> DTO 변환
        ResponseGetClass res = ResponseGetClass.builder()
                .classSeq(result.getClassSeq())
                .classTitle(result.getClassTitle())
                .classContent(result.getClassContent())
                .classState(result.getClassState())
                .classCurrApps(result.getClassCurrApps())
                .classEndDate(result.getClassEndDate())
                .classStartDate(result.getClassStartDate())
                .classPrice(result.getClassPrice())
                .classMaxCap(result.getClassMaxCap())
                .build();

        return res;
    }

    /**
     * 강의 삭제 (soft delete)
     */
    public ClassEntity deleteClass(Long classSeq, String userId) {

        // 1. 강의 유무 확인
        ClassEntity classEntity = classRepository.findByClassSeqAndClassDeletedFalse(classSeq)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // 2. 본인 글인지 확인
        if (!classEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인 글만 삭제 가능");
        }

        // 3. classDeleted: false -> true 변경
        classEntity.deleteClass();

        return classRepository.save(classEntity);
    }

}
