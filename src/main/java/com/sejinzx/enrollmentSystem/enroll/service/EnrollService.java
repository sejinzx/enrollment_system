package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.service.ClassService;
import com.sejinzx.enrollmentSystem.enroll.dto.ResponseGetEnroll;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollEntity;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollState;
import com.sejinzx.enrollmentSystem.enroll.repository.EnrollRepository;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollService {

    private final EnrollRepository enrollRepository;
    private final ClassService classService;
    private final UserService userService;

    /**
     * 수강 신청
     */
    @Transactional
    public EnrollEntity createEnroll(Long classSeq, String userId) {

        // 1. 유저 조회
        UserEntity user = userService.getUser(userId);

        // 2. 강의 조회 및 정원 확인, 증가
        ClassEntity classEntity = classService.validateCapacity(classSeq);

        // 3. 수강 신청 여부 확인
        Optional<EnrollEntity> opt = enrollRepository.findByUser_UserSeqAndClassEntity_ClassSeq(user.getUserSeq(), classSeq);
        EnrollEntity enrollEntity;
        if (opt.isEmpty()) {
            // 4-1. 수강 신청 생성
            enrollEntity = EnrollEntity.builder()
                    .user(user)
                    .classEntity(classEntity)
                    .enrollState(EnrollState.PENDING)
                    .build();
        }
        else if (opt.get().getEnrollState() == EnrollState.CANCELLED) {
            // 4-2. 수강 재신청
            enrollEntity = opt.get();
            enrollEntity.reEnroll();
        }

        else {
            throw new RuntimeException("이미 신청한 강의");
        }

        return enrollRepository.save(enrollEntity);

    }

    /**
     * 수강 신청 취소 (상태 변경)
     */
    public EnrollEntity deleteEnroll(Long enrollSeq, String userId) {

        // 1. 수강 신청 유무 확인
        EnrollEntity enrollEntity = getEnroll(enrollSeq);

        // 2. 본인인지 확인
        if (!enrollEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인 수강 신청만 삭제 가능");
        }

        // 3. enrollState: PENDING or CONFIRMED -> CANCELLED 변경
        enrollEntity.deleteEnroll();

        return enrollRepository.save(enrollEntity);

    }

    /**
     * 수강 신청 유무 확인
     */
    public EnrollEntity getEnroll(Long enrollSeq) {
        return enrollRepository.findById(enrollSeq)
                .orElseThrow(() -> new RuntimeException("Enroll not found"));
    }

    /**
     * 내 수강 신청 목록 확인
     */
    public Page<ResponseGetEnroll> getMyListEnroll(int page, int size, String userId) {

        // 1. 유저 조회
        UserEntity user = userService.getUser(userId);

        // 2. 페이징 조건 설정
        Pageable pageable = PageRequest.of(page, size);

        // 3. 수강 신청한 내용 조회
        Page<EnrollEntity> result = enrollRepository.findByUser_UserSeq(user.getUserSeq(), pageable);

        // 4. Entity -> DTO 변환
        return result.map(enrollEntity -> ResponseGetEnroll.builder()
                .enrollSeq(enrollEntity.getEnrollSeq())
                .enrollState(enrollEntity.getEnrollState())
                .classEntity(enrollEntity.getClassEntity())
                .build());

    }

    /**
     * 결제 후 상태 변경
     * 결제 시스템 연동 시 사용
     */
    public EnrollEntity payedEnroll(Long enrollSeq, String userId) {

        // 1. 수강 신청 유무 확인
        EnrollEntity enrollEntity = getEnroll(enrollSeq);

        // 2. 본인인지 확인
        if (!enrollEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인 수강 신청만 삭제 가능");
        }

        // 3. enrollState: PENDING -> CONFIRMED 변경
        enrollEntity.payedEnroll();

        return enrollRepository.save(enrollEntity);

    }

}
