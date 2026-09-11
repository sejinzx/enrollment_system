package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.config.JwtTokenProvider;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.dto.LoginTokenResponse;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAuthenticationTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtTokenProvider jwt = new JwtTokenProvider("test-only-signing-key-at-least-thirty-two-characters-long");
    private final UserService service = new UserService(repository, encoder, jwt, redis);

    @BeforeEach
    void setup() { when(redis.opsForValue()).thenReturn(values); }

    private void user(UserType role) {
        when(repository.findByUserIdAndUserDeletedFalse("learner")).thenReturn(Optional.of(
                UserEntity.builder().userId("learner").userPw(encoder.encode("password")).userType(role).build()));
    }
    private RequestLogin loginRequest(String password) {
        return RequestLogin.builder().userId("learner").userPw(password).build();
    }
    private void assertError(ErrorCode code, org.junit.jupiter.api.function.Executable action) {
        assertEquals(code, assertThrows(BusinessException.class, action).getErrorCode());
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void loginReturnsBothTokenTypesAndStoresRefreshWithTtl(UserType role) {
        user(role);
        LoginTokenResponse response = service.loginUser(loginRequest("password"));
        assertEquals("learner", jwt.getUserId(response.getAccessToken()));
        assertEquals(role, jwt.getRole(response.getAccessToken()));
        assertEquals("access", jwt.getTokenType(response.getAccessToken()));
        assertEquals("learner", jwt.getUserId(response.getRefreshToken()));
        assertEquals("refresh", jwt.getTokenType(response.getRefreshToken()));
        assertFalse(jwt.isTokenExpired(response.getAccessToken()));
        assertFalse(jwt.isTokenExpired(response.getRefreshToken()));
        assertNotEquals(response.getAccessToken(), response.getRefreshToken());
        verify(values).set("refresh:learner", response.getRefreshToken(),
                jwt.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);
    }

    @Test
    void invalidPasswordDoesNotStoreTokens() {
        user(UserType.CLASSMATE);
        assertError(ErrorCode.INVALID_PASSWORD, () -> service.loginUser(loginRequest("wrong")));
        verifyNoInteractions(values);
    }

    @Test
    void unknownUserDoesNotStoreTokens() {
        when(repository.findByUserIdAndUserDeletedFalse("learner")).thenReturn(Optional.empty());
        assertError(ErrorCode.USER_NOT_FOUND, () -> service.loginUser(loginRequest("password")));
        verifyNoInteractions(values);
    }

    @Test
    void savedRefreshTokenReissuesAccessWithCurrentRole() {
        user(UserType.CREATOR);
        String refresh = jwt.createRefreshToken("learner");
        when(values.get("refresh:learner")).thenReturn(refresh);
        String access = service.reissue(refresh);
        assertEquals("access", jwt.getTokenType(access));
        assertEquals("learner", jwt.getUserId(access));
        assertEquals(UserType.CREATOR, jwt.getRole(access));
    }

    @Test
    void accessTokenCannotBeUsedForReissue() {
        assertError(ErrorCode.INVALID_REFRESH_TOKEN,
                () -> service.reissue(jwt.createAccessToken("learner", "ROLE_CLASSMATE")));
        verifyNoInteractions(values, repository);
    }

    @Test
    void malformedRefreshTokenIsRejected() {
        assertError(ErrorCode.INVALID_REFRESH_TOKEN, () -> service.reissue("invalid-token"));
        verifyNoInteractions(values, repository);
    }

    @Test
    void expiredRefreshTokenIsRejected() {
        JwtTokenProvider expiredJwt = mock(JwtTokenProvider.class);
        when(expiredJwt.isTokenExpired("expired")).thenReturn(true);
        UserService expiredService = new UserService(repository, encoder, expiredJwt, redis);
        assertError(ErrorCode.INVALID_REFRESH_TOKEN, () -> expiredService.reissue("expired"));
        verifyNoInteractions(values, repository);
    }

    @Test
    void missingOrReplacedRefreshTokenIsRejected() {
        String refresh = jwt.createRefreshToken("learner");
        when(values.get("refresh:learner")).thenReturn(null, "different-session-token");
        assertError(ErrorCode.INVALID_REFRESH_TOKEN, () -> service.reissue(refresh));
        assertError(ErrorCode.INVALID_REFRESH_TOKEN, () -> service.reissue(refresh));
        verifyNoInteractions(repository);
    }

    @Test
    void deletedUserCannotReissue() {
        String refresh = jwt.createRefreshToken("learner");
        when(values.get("refresh:learner")).thenReturn(refresh);
        when(repository.findByUserIdAndUserDeletedFalse("learner")).thenReturn(Optional.empty());
        assertError(ErrorCode.USER_NOT_FOUND, () -> service.reissue(refresh));
    }

    @Test
    void logoutDeletesRefreshAndPreventsReissue() {
        user(UserType.CLASSMATE);
        LoginTokenResponse response = service.loginUser(loginRequest("password"));
        when(values.get("refresh:learner")).thenReturn(response.getRefreshToken());
        when(redis.delete("refresh:learner")).thenAnswer(invocation -> {
            when(values.get("refresh:learner")).thenReturn(null);
            return true;
        });
        service.logout(response.getAccessToken());
        verify(redis).delete("refresh:learner");
        assertError(ErrorCode.INVALID_REFRESH_TOKEN, () -> service.reissue(response.getRefreshToken()));
    }

    @Test
    void logoutWithAlreadyMissingRefreshCompletes() {
        when(redis.delete("refresh:learner")).thenReturn(false);
        assertDoesNotThrow(() -> service.logout(jwt.createAccessToken("learner", "ROLE_CLASSMATE")));
        verify(redis).delete("refresh:learner");
    }
}