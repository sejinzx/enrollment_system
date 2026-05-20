package com.sejinzx.enrollmentSystem.user.dto;

import com.sejinzx.enrollmentSystem.user.entity.UserType;
import lombok.Getter;

@Getter
public class RequestAddUser {

    private String userId;
    private String userPw;
    private UserType userType;

}
