package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.config.JwtTokenProvider;
import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder pwEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    public void createUser(RequestAddUser requestAddUser) {

        // 1. 아이디 중복 확인
        if (userRepository.findByUserIdAndUserDeletedFalse(
                requestAddUser.getUserId()).isPresent()) {
            throw new RuntimeException("ID exists");
        }

        // 2. 비밀번호 암호화
        String encodedPw = pwEncoder.encode(requestAddUser.getUserPw());

        // 3. 유저 생성
        UserEntity userEntity = UserEntity.builder()
                .userId(requestAddUser.getUserId())
                .userPw(encodedPw)
                .userType(requestAddUser.getUserType())
                .build();

        // 4. 저장
        UserEntity saved = userRepository.save(userEntity);

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
    public String loginUser(RequestLogin requestLogin) {

        // 1. 유저 조회
        UserEntity user = getUser(requestLogin.getUserId());

        // 2. 비밀번호 확인
        if (!pwEncoder.matches(
                requestLogin.getUserPw(),
                user.getUserPw())) {
            throw new RuntimeException("Password mismatch");
        }

        // 3. JWT 생성
        return jwtTokenProvider.createJwt(
                user.getUserId(),
                "ROLE_" + user.getUserType().name(),
                3600000L
        );

    }

    /**
     * userId로 user 정보 조회
     */
    public UserEntity getUser(String userId) {
        return userRepository.findByUserIdAndUserDeletedFalse(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * User Type 확인
     */
    public UserEntity validateCreator(String userId) {

        // 1. 사용자 유무 확인
        UserEntity user = getUser(userId);

        // 2. 사용자 Type 확인
        if (user.getUserType() != UserType.CREATOR) {
            throw new RuntimeException("권한 없음");
        }

        return user;
    }

}
