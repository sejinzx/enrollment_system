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

    private static final long ACCESS_TOKEN_EXPIRATION =
            1000L * 60 * 30; // 30분

    private static final long REFRESH_TOKEN_EXPIRATION =
            1000L * 60 * 60 * 24 * 7; // 7일

    /**
     * SecretKey 생성
     */
    public JwtTokenProvider(
            @Value("${spring.jwt.secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String userId, String role) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .claim("tokenType", "access")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + ACCESS_TOKEN_EXPIRATION
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String userId) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("tokenType", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + REFRESH_TOKEN_EXPIRATION
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    public long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION;
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
     * Token Type 추출
     */
    public String getTokenType(String token) {
        return parseClaims(token)
                .get("tokenType", String.class);
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
     * 남은 만료 시간
     */
    public long getRemainingExpiration(String token) {

        Date expiration =
                parseClaims(token).getExpiration();

        return expiration.getTime()
                - System.currentTimeMillis();
    }

    /**
     * JWT Claims 파싱
     */
    private Claims parseClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}