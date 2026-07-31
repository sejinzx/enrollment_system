package com.sejinzx.enrollmentSystem.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_seq", nullable = false)
    private Long userSeq;

    @Column(name = "user_id", nullable = false, length = 30, unique = true)
    private String userId;

    @Column(name = "user_pw", nullable = false)
    private String userPw;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private UserType userType;

    @Column(name = "user_create_date", nullable = false)
    @CreatedDate
    private LocalDateTime userCreateDate;

    @Column(name = "user_update_date", nullable = false)
    @LastModifiedDate
    private LocalDateTime userUpdateDate;

    @Column(name = "user_deleted", nullable = false)
    private boolean userDeleted = false;

    @Builder
    public UserEntity(String userId, String userPw, UserType userType){
        this.userId = userId;
        this.userPw = userPw;
        this.userType = userType;
    }

}
