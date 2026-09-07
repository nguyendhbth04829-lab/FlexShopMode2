package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${flexshop.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D6351}")
    private String jwtSecret;

    @Value("${flexshop.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs; // 15 phút

    @Value("${flexshop.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs; // 7 ngày

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(NguoiDungPrincipal nguoiDungPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        List<String> roles = nguoiDungPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(nguoiDungPrincipal.getEmail())
                .claim("userId", nguoiDungPrincipal.getMaNguoiDung())
                .claim("hoVaTen", nguoiDungPrincipal.getHoVaTen())
                .claim("soDienThoai", nguoiDungPrincipal.getSoDienThoai())
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(NguoiDungPrincipal nguoiDungPrincipal, String tokenId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(nguoiDungPrincipal.getEmail())
                .id(tokenId != null ? tokenId : UUID.randomUUID().toString())
                .claim("userId", nguoiDungPrincipal.getMaNguoiDung())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type", String.class);
    }

    public String getTokenId(String token) {
        return getClaimsFromToken(token).getId();
    }

    public long getRemainingExpirationMs(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            long diff = expiration.getTime() - System.currentTimeMillis();
            return Math.max(diff, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Chữ ký JWT không hợp lệ: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT Token đã hết hạn: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT Token không được hỗ trợ: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims rỗng hoặc không hợp lệ: {}", e.getMessage());
        }
        return false;
    }
}
