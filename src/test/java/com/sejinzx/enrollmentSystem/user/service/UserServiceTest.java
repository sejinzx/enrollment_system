package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.error.BusinessException;
import com.sejinzx.enrollmentSystem.error.ErrorCode;
import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @BeforeEach
    void before() {
        System.out.println("===== 테스트 시작 =====");
    }

    @AfterEach
    void after() {
        System.out.println("===== 테스트 종료 =====");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder pwEncoder;

    /*
     * 회원가입 성공 테스트
     */
    @Test
    void createUser_success_test() {

        // given
        RequestAddUser request = RequestAddUser.builder()
                .userId("testUser")
                .userPw("1234")
                .userType(UserType.CLASSMATE)
                .build();

        // when
        userService.createUser(request);

        // then
        UserEntity result =
                userRepository
                        .findByUserIdAndUserDeletedFalse("testUser")
                        .orElseThrow();

        Assertions.assertEquals(
                "testUser",
                result.getUserId()
        );

        Assertions.assertTrue(
                pwEncoder.matches(
                        "1234",
                        result.getUserPw()
                )
        );

        System.out.println("회원가입 성공");
    }

    /*
     * 회원가입 실패 테스트
     * 예상 결과: 중복 아이디 예외 발생
     */
    @Test
    void createUser_fail_test() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("existsUser")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        RequestAddUser request =
                RequestAddUser.builder()
                        .userId("existsUser")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build();

        // when
        BusinessException exception =
                Assertions.assertThrows(
                        BusinessException.class,
                        () -> userService.createUser(request)
                );

        // then
        Assertions.assertEquals(
                ErrorCode.DUPLICATE_USER_ID,
                exception.getErrorCode()
        );

        System.out.println("회원가입 실패 - 중복 아이디");
    }

    /*
     * 로그인 성공 테스트
     */
    @Test
    void loginUser_success_test() {

        // given
        String encodedPw =
                pwEncoder.encode("1234");

        userRepository.save(
                UserEntity.builder()
                        .userId("loginUser1")
                        .userPw(encodedPw)
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        RequestLogin request =
                RequestLogin.builder()
                        .userId("loginUser1")
                        .userPw("1234")
                        .build();

        // when
        String token =
                userService.loginUser(request);

        // then
        Assertions.assertNotNull(token);

        System.out.println("로그인 성공");
        System.out.println("JWT Token = " + token);
    }

    /*
     * 로그인 실패 테스트
     * 예상 결과: 비밀번호 불일치
     */
    @Test
    void loginUser_fail_test() {

        // given
        String encodedPw =
                pwEncoder.encode("1234");

        userRepository.save(
                UserEntity.builder()
                        .userId("loginUser2")
                        .userPw(encodedPw)
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        RequestLogin request =
                RequestLogin.builder()
                        .userId("loginUser2")
                        .userPw("wrongPw")
                        .build();

        // when
        BusinessException exception =
                Assertions.assertThrows(
                        BusinessException.class,
                        () -> userService.loginUser(request)
                );

        // then
        Assertions.assertEquals(
                ErrorCode.INVALID_PASSWORD,
                exception.getErrorCode()
        );

        System.out.println("로그인 실패 - 비밀번호 불일치");
    }

    /*
     * Creator 권한 실패 테스트
     * 예상 결과: 권한 없음
     */
    @Test
    void validateCreator_fail_test() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("classmate")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        // when
        BusinessException exception =
                Assertions.assertThrows(
                        BusinessException.class,
                        () -> userService.validateCreator("classmate")
                );

        // then
        Assertions.assertEquals(
                ErrorCode.FORBIDDEN,
                exception.getErrorCode()
        );

        System.out.println("Creator 권한 검증 실패");
    }

    /*
     * Classmate 권한 실패 테스트
     * 예상 결과: 권한 없음
     */
    @Test
    void validateClassmate_fail_test() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        // when
        BusinessException exception =
                Assertions.assertThrows(
                        BusinessException.class,
                        () -> userService.validateClassmate("creator")
                );

        // then
        Assertions.assertEquals(
                ErrorCode.FORBIDDEN,
                exception.getErrorCode()
        );

        System.out.println("Classmate 권한 검증 실패");
    }
}