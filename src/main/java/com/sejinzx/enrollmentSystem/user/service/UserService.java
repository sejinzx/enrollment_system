package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.config.JwtTokenProvider;
import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder pwEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    public ResponseEntity<?> createUser(RequestAddUser requestAddUser) {

        String encodedPw = pwEncoder.encode(requestAddUser.getUserPw());

        UserEntity userEntity = UserEntity.builder()
                .userId(requestAddUser.getUserId())
                .userPw(encodedPw)
                .userType(requestAddUser.getUserType())
                .build();

        UserEntity saved = userRepository.save(userEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);

    }

    /**
     * 아이디 중복 확인
     */
    public boolean existsById(String id) {
        // 존재 -> true, 존재 X -> false
        return userRepository.findById(id).isPresent();
    }

    /*
    * 로그인
    */
    public ResponseEntity<?> loginUser(RequestLogin requestLogin) {
        // 아이디 확인
        UserEntity user = userRepository.findById(requestLogin.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 비밀번호 확인
        if (!pwEncoder.matches(requestLogin.getUserPw(), user.getUserPw())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("Message", "Password mismatch"));
        }

        // jwt 토큰 생성
        String accessToken = jwtTokenProvider.createJwt(
                user.getUserId(),
                "ROLE_" + user.getUserType().name(),
                3600000L // 1시간 (60 * 60 * 1000)
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of(
                        "Message", "Login success",
                        "AccessToken", accessToken
                ));
    }

}
