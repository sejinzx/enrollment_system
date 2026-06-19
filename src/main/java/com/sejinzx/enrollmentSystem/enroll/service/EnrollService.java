package com.sejinzx.enrollmentSystem.enroll.service;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.classmgmt.service.ClassService;
import com.sejinzx.enrollmentSystem.enroll.dto.EnrollmentRequestedEvent;
import com.sejinzx.enrollmentSystem.enroll.dto.ResponseGetEnroll;
import com.sejinzx.enrollmentSystem.enroll.dto.ResponseGetUserEnrollClass;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollEntity;
import com.sejinzx.enrollmentSystem.enroll.entity.EnrollState;
import com.sejinzx.enrollmentSystem.enroll.kafka.EnrollmentProducer;
import com.sejinzx.enrollmentSystem.enroll.repository.EnrollRepository;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollService {

    private final EnrollRepository enrollRepository;
    private final ClassService classService;
    private final UserService userService;
    private final EnrollmentProducer enrollmentProducer;

    /**
     * Kafka 요청 발행
     */
    public void requestEnroll(Long classSeq, String userId) {

        enrollmentProducer.sendRequest(new EnrollmentRequestedEvent(userId, classSeq));
    }

    /**
     * 수강 신청
     */
    @Transactional
    public Long processEnroll(Long classSeq, String userId) {

        log.info("🔥 PROCESS ENROLL START");

        // 1. 유저 조회
        UserEntity user = userService.validateClassmate(userId);

        // 2. 강의 조회 및 정원 확인, 증가
        ClassEntity classEntity = classService.getClass(classSeq);

        // 3. 수강 신청 여부 확인
        Optional<EnrollEntity> opt =
                enrollRepository.findByUser_UserSeqAndClassEntity_ClassSeq(user.getUserSeq(), classSeq);

        EnrollEntity enrollEntity;

        // 4-1. 신규 신청
        if (opt.isEmpty()) {
            enrollEntity = EnrollEntity.builder()
                    .user(user)
                    .classEntity(classEntity)
                    .enrollState(EnrollState.PENDING)
                    .build();
        }
        // 4-2. 취소 후 재신청
        else if (opt.get().getEnrollState() == EnrollState.CANCELLED) {
            enrollEntity = opt.get();
            enrollEntity.reEnroll();
        }
        // 4-3. 신청 이력 있음
        else {
            throw new BusinessException(ErrorCode.DUPLICATE_ENROLL);
        }

        return enrollRepository.save(enrollEntity)
                .getEnrollSeq();
    }

    /**
     * 수강 신청 취소 (상태 변경)
     */
    @Transactional
    public Long deleteEnroll(Long enrollSeq, String userId) {

        // 1. 수강 신청 유무 확인
        EnrollEntity enrollEntity = validateMyEnroll(enrollSeq, userId);

        // 2. 취소 검증
        validateCancelable(enrollEntity);

        // 3. 취소
        enrollEntity.deleteEnroll();

        return enrollEntity.getEnrollSeq();
    }

    /**
     * 수강 신청 유무 확인
     */
    public EnrollEntity getEnroll(Long enrollSeq) {

        return enrollRepository.findById(enrollSeq)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLL_NOT_FOUND));
    }

    /**
     * 내 수강 신청 목록 확인
     */
    public Page<ResponseGetEnroll> getMyListEnroll(int page, int size, String userId) {

        // 1. 유저 조회
        Long userSeq = userService.validateClassmate(userId).getUserSeq();

        // 2. 페이징 조건 설정
        Pageable pageable = PageRequest.of(page, size);

        return enrollRepository
                .findByUser_UserSeq(userSeq, pageable)
                .map(enroll -> ResponseGetEnroll.builder()
                        .enrollSeq(enroll.getEnrollSeq())
                        .enrollState(enroll.getEnrollState())
                        .classTitle(enroll.getClassEntity().getClassTitle())
                        .classStartDate(enroll.getClassEntity().getClassStartDate())
                        .classEndDate(enroll.getClassEntity().getClassEndDate())
                        .build()
                );
    }

    /**
     * 결제 후 상태 변경
     * 결제 시스템 연동 시 사용
     */
    @Transactional
    public Long payedEnroll(Long enrollSeq, String userId) {

        // 1. 수강 신청 유무 확인
        EnrollEntity enrollEntity = validateMyEnroll(enrollSeq, userId);

        // 2. enrollState: PENDING -> CONFIRMED 변경
        enrollEntity.payedEnroll();

        return enrollEntity.getEnrollSeq();
    }

    /**
     * 강의 별 수강생 목록 조회
     */
    public Page<ResponseGetUserEnrollClass> getClassEnrollUserList(int page, int size, Long classSeq, String userId) {

        // 1. 사용자 검증
        Long userSeq = userService.validateCreator(userId).getUserSeq();

        // 2. 본인 강의 검증
        classService.getValidateMyClass(classSeq, userSeq);

        // 3. 페이징 조건 설정
        Pageable pageable = PageRequest.of(page, size);

        // 4. 수강 신청 목록 조회
        return enrollRepository
                .findByClassEntity_ClassSeqAndEnrollState(classSeq, EnrollState.CONFIRMED, pageable)
                .map(enroll -> ResponseGetUserEnrollClass.builder()
                        .enrollSeq(enroll.getEnrollSeq())
                        .userId(enroll.getUser().getUserId())
                        .build()
                );
    }

    /**
     * 본인 수강 신청 검증
     */
    private EnrollEntity validateMyEnroll(Long enrollSeq, String userId) {

        EnrollEntity enrollEntity = getEnroll(enrollSeq);

        if (!enrollEntity.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return enrollEntity;
    }

    /**
     * 취소 가능 여부 검증
     */
    private void validateCancelable(EnrollEntity enrollEntity) {

        // 결제 전 취소 가능
        if (enrollEntity.getEnrollState() == EnrollState.PENDING) {
            return;
        }

        // 결제 후 3일 지났을 시 취소 불가
        if(enrollEntity.getEnrollState() == EnrollState.CONFIRMED &&
                LocalDate.now().isAfter(enrollEntity.getEnrollUpdateDate().plusDays(3))) {
            throw new BusinessException(ErrorCode.CANCEL_PERIOD_EXPIRED);
        }
    }



}
