package com.sejinzx.enrollmentSystem.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginTokenResponse {

    private String accessToken;
    private String refreshToken;
}
