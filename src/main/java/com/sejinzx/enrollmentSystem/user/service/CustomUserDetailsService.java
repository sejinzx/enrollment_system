package com.sejinzx.enrollmentSystem.user.service;

import com.sejinzx.enrollmentSystem.user.entity.CustomUserDetails;
import com.sejinzx.enrollmentSystem.user.entity.UserEntity;
import com.sejinzx.enrollmentSystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * userId를 이용해 사용자 정보를 조회
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Optional<UserEntity> userOptional = userRepository.findById(userId);

        UserEntity user = userOptional.orElseThrow(() -> {
            log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
            return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        });

        return new CustomUserDetails(user);

    }
}