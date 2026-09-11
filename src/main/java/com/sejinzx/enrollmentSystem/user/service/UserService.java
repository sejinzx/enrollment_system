package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.config.JwtTokenProvider;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.dto.LoginTokenResponse;
import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder pwEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    /**
     * 회원가입
     */
    public void createUser(RequestAddUser requestAddUser) {

        // 1. 아이디 중복 확인
        validateDuplicateUser(requestAddUser.getUserId());

        // 2. 비밀번호 암호화
        String encodedPw = pwEncoder.encode(requestAddUser.getUserPw());

        // 3. 유저 생성
        UserEntity userEntity = UserEntity.builder()
                .userId(requestAddUser.getUserId())
                .userPw(encodedPw)
                .userType(requestAddUser.getUserType())
                .build();

        // 4. 저장
        userRepository.save(userEntity);
    }

    /**
     * 아이디 중복 확인
     */
    public void validateDuplicateUser(String userId) {

        if (userRepository.existsByUserIdAndUserDeletedFalse(userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_USER_ID);
        }
    }

    /**
     * 로그인
     */
    public LoginTokenResponse loginUser(RequestLogin requestLogin) {

        UserEntity user = findActiveUser(requestLogin.getUserId());

        if (!pwEncoder.matches(requestLogin.getUserPw(), user.getUserPw())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                "ROLE_" + user.getUserType().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getUserId()
        );

        redisTemplate.opsForValue().set(
                "refresh:" + user.getUserId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return new LoginTokenResponse(
                accessToken,
                refreshToken
        );
    }

    /**
     * Access Token 재발급
     */
    public String reissue(String refreshToken) {

        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtTokenProvider.getUserId(refreshToken);

        String savedRefreshToken =
                redisTemplate.opsForValue()
                        .get("refresh:" + userId);

        if (savedRefreshToken == null ||
                !savedRefreshToken.equals(refreshToken)) {

            throw new BusinessException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        UserEntity user = findActiveUser(userId);

        return jwtTokenProvider.createAccessToken(
                user.getUserId(),
                "ROLE_" + user.getUserType().name()
        );
    }

    /**
     * 로그아웃
     */
    public void logout(String accessToken) {

        String userId = jwtTokenProvider.getUserId(accessToken);

        redisTemplate.delete("refresh:" + userId);
    }

    /**
     * userId로 user 정보 조회
     */
    public UserEntity findActiveUser(String userId) {

        return userRepository.findByUserIdAndUserDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Creator 확인
     */
    public UserEntity validateCreator(String userId) {

        // 1. 사용자 유무 확인
        UserEntity user = findActiveUser(userId);

        // 2. 사용자 Type 확인
        if (user.getUserType() != UserType.CREATOR) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return user;
    }

    /**
     * Classmate 확인
     */
    public UserEntity validateClassmate(String userId) {

        // 1. 사용자 유무 확인
        UserEntity user = findActiveUser(userId);

        // 2. 사용자 Type 확인
        if (user.getUserType() != UserType.CLASSMATE) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return user;
    }
}
