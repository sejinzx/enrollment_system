package com.sejinzx.enrollmentSystem.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RequestLogin {

    private String userId;
    private String userPw;

    @Builder
    public RequestLogin(String userId, String userPw) {
        this.userId = userId;
        this.userPw = userPw;
    }
}
