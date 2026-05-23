package com.sejinzx.enrollmentSystem.user;

import com.sejinzx.enrollmentSystem.user.dto.RequestAddUser;
import com.sejinzx.enrollmentSystem.user.dto.RequestLogin;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.entity.UserType;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import com.sejinzx.enrollmentSystem.user.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder pwEncoder;

    /*
     * 회원가입 테스트
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
     * 아이디 중복 확인 테스트
     */
    @Test
    void existsById_test() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("existsUser")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        // when
        boolean result =
                userService.existsById("existsUser");

        // then
        Assertions.assertTrue(result);
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
     * 사용자 조회 테스트
     */
    @Test
    void getUser_test() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("findUser")
                        .userPw("1234")
                        .userType(UserType.CLASSMATE)
                        .build()
        );

        // when
        UserEntity result =
                userService.getUser("findUser");

        // then
        Assertions.assertEquals(
                "findUser",
                result.getUserId()
        );
    }

    /*
     * Creator 권한 확인 테스트
     */
    @Test
    void validateCreator_test() {

        // given
        userRepository.save(
                UserEntity.builder()
                        .userId("creator")
                        .userPw("1234")
                        .userType(UserType.CREATOR)
                        .build()
        );

        // when
        UserEntity result =
                userService.validateCreator("creator");

        // then
        Assertions.assertEquals(
                UserType.CREATOR,
                result.getUserType()
        );
    }

    /*
     * Creator 권한 실패 테스트
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
