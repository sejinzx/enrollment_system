package com.sejinzx.enrollmentSystem.user.dto;

import com.sejinzx.enrollmentSystem.user.entity.UserType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RequestAddUser {

    private String userId;
    private String userPw;
    private UserType userType;

    @Builder
    public RequestAddUser(String userId, String userPw, UserType userType) {
        this.userId = userId;
        this.userPw = userPw;
        this.userType = userType;
    }
}
