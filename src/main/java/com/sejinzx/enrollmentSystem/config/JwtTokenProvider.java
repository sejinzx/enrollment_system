package com.sejinzx.enrollmentSystem.config;

import com.sejinzx.enrollmentSystem.user.entity.UserType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    /**
     * SecretKey 생성
     */
    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * JWT 생성
     */
    public String createJwt(String userId, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * userId 추출
     */
    public String getUserId(String token) {
        return parseClaims(token)
                .get("userId", String.class);
    }

    /**
     * role 추출
     */
    public UserType getRole(String token) {
        String role = parseClaims(token)
                .get("role", String.class);

        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        try {
            return UserType.valueOf(role);
        } catch (Exception e) {
            log.error("잘못된 role: {}", role);
            throw new JwtException("Invalid role");
        }
    }

    /**
     * 만료 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token)
                    .getExpiration()
                    .before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 공통 파싱 메서드 (핵심)
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}