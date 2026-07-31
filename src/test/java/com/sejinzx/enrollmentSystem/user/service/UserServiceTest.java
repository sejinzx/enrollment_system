package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.MySqlContainerTest;
import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest extends MySqlContainerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder pwEncoder;

    @Test
    @DisplayName("회원가입 성공")
    void createUser_success() {

        // given
        RequestAddUser request = RequestAddUser.builder()
                .userId("testUser")
                .userPw("1234")
                .userType(UserType.CLASSMATE)
                .build();

        // when
        userService.createUser(request);

        // then
        UserEntity result = userRepository
                .findByUserIdAndUserDeletedFalse("testUser")
                .orElseThrow();

        assertAll(
                () -> assertEquals("testUser", result.getUserId()),
                () -> assertEquals(UserType.CLASSMATE, result.getUserType()),
                () -> assertTrue(
                        pwEncoder.matches("1234", result.getUserPw())
                )
        );
    }

    @Test
    @DisplayName("중복된 아이디로 회원가입 시 예외 발생")
    void createUser_duplicateId_throwsException() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("existsUser")
                        .userPw(pwEncoder.encode("1234"))
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        RequestAddUser request = RequestAddUser.builder()
                .userId("existsUser")
                .userPw("1234")
                .userType(UserType.CLASSMATE)
                .build();

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(request)
        );

        // then
        assertEquals(
                ErrorCode.DUPLICATE_USER_ID,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("로그인 성공")
    void loginUser_success() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("loginUser1")
                        .userPw(pwEncoder.encode("1234"))
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        RequestLogin request = RequestLogin.builder()
                .userId("loginUser1")
                .userPw("1234")
                .build();

        // when
        String token = userService.loginUser(request);

        // then
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("비밀번호 불일치 시 로그인 실패")
    void loginUser_passwordMismatch_throwsException() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("loginUser2")
                        .userPw(pwEncoder.encode("1234"))
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        RequestLogin request = RequestLogin.builder()
                .userId("loginUser2")
                .userPw("wrongPw")
                .build();

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.loginUser(request)
        );

        // then
        assertEquals(
                ErrorCode.INVALID_PASSWORD,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 시 예외 발생")
    void loginUser_userNotFound_throwsException() {

        // given
        RequestLogin request = RequestLogin.builder()
                .userId("unknownUser")
                .userPw("1234")
                .build();

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.loginUser(request)
        );

        // then
        assertEquals(
                ErrorCode.USER_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Creator 사용자 권한 검증 성공")
    void validateCreator_success() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw(pwEncoder.encode("1234"))
                        .userType(UserType.CREATOR)
                        .build()
        );

        // when
        UserEntity result = userService.validateCreator("creator");

        // then
        assertAll(
                () -> assertEquals("creator", result.getUserId()),
                () -> assertEquals(UserType.CREATOR, result.getUserType())
        );
    }

    @Test
    @DisplayName("Creator가 아닌 사용자 권한 검증 시 예외 발생")
    void validateCreator_wrongUserType_throwsException() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("classmate")
                        .userPw(pwEncoder.encode("1234"))
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.validateCreator("classmate")
        );

        // then
        assertEquals(
                ErrorCode.FORBIDDEN,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Classmate 사용자 권한 검증 성공")
    void validateClassmate_success() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("classmate")
                        .userPw(pwEncoder.encode("1234"))
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        // when
        UserEntity result = userService.validateClassmate("classmate");

        // then
        assertAll(
                () -> assertEquals("classmate", result.getUserId()),
                () -> assertEquals(UserType.CLASSMATE, result.getUserType())
        );
    }

    @Test
    @DisplayName("Classmate가 아닌 사용자 권한 검증 시 예외 발생")
    void validateClassmate_wrongUserType_throwsException() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw(pwEncoder.encode("1234"))
                        .userType(UserType.CREATOR)
                        .build()
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.validateClassmate("creator")
        );

        // then
        assertEquals(
                ErrorCode.FORBIDDEN,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void findActiveUser_userNotFound_throwsException() {

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.findActiveUser("unknownUser")
        );

        // then
        assertEquals(
                ErrorCode.USER_NOT_FOUND,
                exception.getErrorCode()
        );
    }
}