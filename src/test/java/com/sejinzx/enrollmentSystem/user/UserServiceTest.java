package com.sejinzx.enrollmentSystem.user;

import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import com.sejinzx.enrollmentSystem.user.service.UserService;
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
     * 회원가입
     */
    @Test
    void createUser_test() {

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

    }

    /*
     * 회원가입 실패 테스트
     * 예상 결과: ID exists
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

        // when & then
        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> userService.createUser(request)
                );

        Assertions.assertEquals(
                "ID exists",
                exception.getMessage()
        );
    }

    /*
     * 로그인 테스트
     */
    @Test
    void loginUser_test() {

        // given
        String encodedPw =
                pwEncoder.encode("1234");

        userRepository.save(
                UserEntity.builder()
                        .userId("loginUser")
                        .userPw(encodedPw)
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        RequestLogin request =
                RequestLogin.builder()
                        .userId("loginUser")
                        .userPw("1234")
                        .build();

        // when
        String token =
                userService.loginUser(request);

        // then
        Assertions.assertNotNull(token);

        System.out.println("JWT Token = " + token);

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
                        .userId("student")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        // when & then
        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> userService.validateCreator("student")
                );

        Assertions.assertEquals(
                "권한 없음",
                exception.getMessage()
        );

    }

}
