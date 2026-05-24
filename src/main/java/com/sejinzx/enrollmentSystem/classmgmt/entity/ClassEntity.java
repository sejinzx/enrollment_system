package com.sejinzx.enrollmentSystem.classmgmt.entity;

import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "classes")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_seq", nullable = false)
    private Long classSeq;

    @Column(name = "class_title", nullable = false)
    private String classTitle;

    @Column(name = "class_content", nullable = false, columnDefinition = "TEXT")
    private String classContent;

    @Column(name = "class_price", nullable = false)
    private BigDecimal classPrice;

    @Column(name = "class_max_cap", nullable = false)
    private int classMaxCap;

    @Column(name = "class_curr_apps", nullable = false)
    private int classCurrApps = 0;

    @Column(name = "class_start_date", nullable = false)
    private LocalDate classStartDate;

    @Column(name = "class_end_date", nullable = false)
    private LocalDate classEndDate;

    @Column(name = "class_state", nullable = false, length = 20)
    private ClassState classState;

    @Column(name = "class_create_date", nullable = false)
    @CreatedDate
    private LocalDate classCreateDate;

    @Column(name = "class_update_date", nullable = false)
    @LastModifiedDate
    private LocalDate classUpdateDate;

    @Column(name = "class_deleted", nullable = false)
    private boolean classDeleted = false;

    @ManyToOne
    @JoinColumn(name = "user_seq")
    private UserEntity user;

    @Builder
    public ClassEntity(String classTitle, String classContent, BigDecimal classPrice,
                       int classMaxCap, LocalDate classStartDate, LocalDate classEndDate,
                       ClassState classState, UserEntity user) {
        this.classTitle = classTitle;
        this.classContent = classContent;
        this.classPrice = classPrice;
        this.classMaxCap = classMaxCap;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
        this.classState = classState;
        this.user = user;
    }

    /**
     * 강의 수정 메서드
     */
    public void updateClass(String classTitle, String classContent, BigDecimal classPrice,
                            int classMaxCap, LocalDate classStartDate, LocalDate classEndDate) {
        this.classTitle = classTitle;
        this.classContent = classContent;
        this.classPrice = classPrice;
        this.classMaxCap = classMaxCap;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
    }

    /**
     * 강의 삭제 메서드
     */
    public void deleteClass() {
        this.classDeleted = true;
    }

    /**
     * 현재 신청 인원 증가
     */
    public void increaseCurrApps() {
        this.classCurrApps++;
    }

    /**
     * 모집 시작
     */
    public void openClass() {
        this.classState = ClassState.OPEN;
    }

    /**
     * 모집 종료
     */
    public void closeClass() {
        this.classState = ClassState.CLOSED;
    }

    /**
     * 모집 상태 변경
     */
    public void updateClassState(ClassState classState) { this.classState = classState; }

    /**
     * 테스트용 메서드
     */
    public void changeCurrApps(int currApps) {
        this.classCurrApps = currApps;
    }
}
