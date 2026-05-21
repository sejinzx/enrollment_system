package com.sejinzx.enrollmentSystem.user.controller;

import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SecurityRequirements
public class UserController {

    private final UserService userService;

    @Tag(name = "User 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody RequestAddUser requestAddUser) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(requestAddUser));
    }

    @Tag(name = "User ID 중복 확인")
    @Description("userId 존재 -> true, userId 존재 X -> false")
    @GetMapping("/existsById")
    public ResponseEntity<?> existsById(@RequestParam String id) {

        return ResponseEntity.ok(userService.existsById(id));
    }

    @Tag(name = "User 로그인")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody RequestLogin requestLogin) {

        return ResponseEntity.ok(
                userService.loginUser(requestLogin)
        );
    }
}
