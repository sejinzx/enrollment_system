package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.config.JwtTokenProvider;
import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    public UserEntity createUser(RequestAddUser requestAddUser) {

        // 중복 확인
        if (userRepository.findByUserIdAndUserDeletedFalse(
                requestAddUser.getUserId()).isPresent()) {

            throw new RuntimeException("이미 존재하는 ID");
        }

        // 비밀번호 암호화
        String encodedPw = pwEncoder.encode(requestAddUser.getUserPw());

        // Entity 생성
        UserEntity userEntity = UserEntity.builder()
                .userId(requestAddUser.getUserId())
                .userPw(encodedPw)
                .userType(requestAddUser.getUserType())
                .build();

        // 저장
        return userRepository.save(userEntity);
    }

    /**
     * 아이디 중복 확인
     */
    public boolean existsById(String id) {

        return userRepository
                .findByUserIdAndUserDeletedFalse(id)
                .isPresent();
    }

    /**
     * 로그인
     */
    public Map<String, String> loginUser(RequestLogin requestLogin) {

        // 유저 조회
        UserEntity user = getUser(requestLogin.getUserId());

        // 비밀번호 확인
        if (!pwEncoder.matches(
                requestLogin.getUserPw(),
                user.getUserPw())) {

            throw new RuntimeException("Password mismatch");
        }

        // JWT 생성
        String accessToken = jwtTokenProvider.createJwt(
                user.getUserId(),
                "ROLE_" + user.getUserType().name(),
                3600000L
        );

        // 응답 반환
        return Map.of(
                "message", "Login success",
                "accessToken", accessToken
        );
    }

    /**
     * userId로 user 정보 조회
     */
    public UserEntity getUser(String userId) {
        return userRepository.findByUserIdAndUserDeletedFalse(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
