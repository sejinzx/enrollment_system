package com.sejinzx.enrollmentSystem.user.controller;

import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirements
public class UserController {

    private final UserService userService;

    @Tag(name = "User 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody RequestAddUser requestAddUser) {

        userService.createUser(requestAddUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message: ", "signup success"));
    }

    @Tag(name = "User ID 중복 확인")
    @Operation(description = "userId 존재 -> true, userId 존재 X -> false")
    @GetMapping("/exists")
    public ResponseEntity<?> existsById(@RequestParam String id) {

        userService.validateDuplicateUser(id);

        return ResponseEntity.ok(Map.of("message: ", "available ID"));
    }

    @Tag(name = "User 로그인")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody RequestLogin requestLogin) {

        return ResponseEntity.ok(
                Map.of("accessToken", userService.loginUser(requestLogin),
                        "message", "login success")
        );
    }
}
