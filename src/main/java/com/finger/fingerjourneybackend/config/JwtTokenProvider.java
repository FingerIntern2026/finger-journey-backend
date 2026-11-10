package com.finger.fingerjourneybackend.config;

import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String createAccessToken(Long employeeId) {
        return createToken(employeeId, "ACCESS", accessTokenExpirationSeconds);
    }

    public String createRefreshToken(Long employeeId) {
        return createToken(employeeId, "REFRESH", refreshTokenExpirationSeconds);
    }

    public Long validateAccessToken(String token) {
        return validateToken(token, "ACCESS");
    }

    public Long validateRefreshToken(String token) {
        return validateToken(token, "REFRESH");
    }

    // 서명과 만료를 확인한 뒤, 용도가 맞는 토큰에서만 직원 ID를 반환한다.
    private Long validateToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!expectedType.equals(claims.get("tokenType", String.class))
                    || claims.getExpiration() == null) {
                throw new CustomException(ErrorCode.INVALID_SESSION);
            }

            Long employeeId = Long.valueOf(claims.getSubject());
            if (employeeId <= 0) {
                throw new CustomException(ErrorCode.INVALID_SESSION);
            }
            return employeeId;
        } catch (ExpiredJwtException e) {
            throw new CustomException("REFRESH".equals(expectedType)
                    ? ErrorCode.REFRESH_TOKEN_EXPIRED : ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_SESSION);
        }
    }

    private String createToken(Long employeeId, String tokenType, long expirationSeconds) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(employeeId.toString())
                .claim("tokenType", tokenType)
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
}
