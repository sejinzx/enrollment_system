package com.sejinzx.enrollmentSystem.enroll.entity;

import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "enrollments")
public class EnrollEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enroll_seq", nullable = false)
    private Long enrollSeq;

    @Column(name = "enroll_state", nullable = false, length = 20)
    private EnrollState enrollState;

    @Column(name = "enroll_create_date", nullable = false)
    @CreatedDate
    private LocalDate enrollCreateDate;

    @Column(name = "enroll_update_date", nullable = false)
    @LastModifiedDate
    private LocalDate enrollUpdateDate;

    @ManyToOne
    @JoinColumn(name = "user_seq")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "class_seq")
    private ClassEntity classEntity;

    @Builder
    public EnrollEntity(EnrollState enrollState, UserEntity user, ClassEntity classEntity) {
        this.enrollState = enrollState;
        this.user = user;
        this.classEntity = classEntity;
    }

    /**
     * 수강 신청 삭제 메서드
     */
    public void deleteEnroll() {
        this.enrollState = EnrollState.CANCELLED;
    }

    /**
     * 수강 재신청 메서드
     */
    public void reEnroll() {
        this.enrollState = EnrollState.PENDING;
    }

    /**
     * 수강 신청 결제 확정 메서드
     */
    public void payedEnroll() {
        this.enrollState = EnrollState.CONFIRMED;
    }

    /**
     * 테스트용 수정일 변경
     */
    public void changeUpdateDate(LocalDate date) {
        this.enrollUpdateDate = date;
    }

}
