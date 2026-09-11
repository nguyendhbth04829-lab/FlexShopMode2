package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenRedisService.class);

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "flexshop:refresh_token:";
    private static final String BLACKLIST_TOKEN_PREFIX = "flexshop:blacklist_token:";

    // Bộ nhớ đệm Fallback an toàn phòng khi Redis server chưa khởi động cục bộ
    private final Map<String, TokenEntry> fallbackStore = new ConcurrentHashMap<>();

    private static class TokenEntry {
        final String value;
        final long expireAtMillis;

        TokenEntry(String value, long durationMs) {
            this.value = value;
            this.expireAtMillis = System.currentTimeMillis() + durationMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAtMillis;
        }
    }

    /**
     * Lưu Refresh Token vào Redis với thời hạn hết hạn 7 ngày
     */
    public void saveRefreshToken(Long userId, String tokenId, String refreshToken, long durationMs) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + tokenId;
        try {
            redisTemplate.opsForValue().set(key, refreshToken, durationMs, TimeUnit.MILLISECONDS);
            log.info("Đã lưu Refresh Token vào Redis cho userId: {}, tokenId: {}", userId, tokenId);
        } catch (Exception e) {
            log.warn("Không kết nối được Redis, lưu tạm Refresh Token vào bộ nhớ dự phòng: {}", e.getMessage());
            fallbackStore.put(key, new TokenEntry(refreshToken, durationMs));
        }
    }

    /**
     * Kiểm tra tính hợp lệ của Refresh Token trong Redis
     */
    public boolean validateRefreshToken(Long userId, String tokenId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + tokenId;
        try {
            String storedToken = redisTemplate.opsForValue().get(key);
            if (storedToken != null) {
                return storedToken.equals(refreshToken);
            }
        } catch (Exception e) {
            log.warn("Redis không phản hồi, kiểm tra trong bộ nhớ dự phòng: {}", e.getMessage());
        }

        TokenEntry entry = fallbackStore.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                fallbackStore.remove(key);
                return false;
            }
            return entry.value.equals(refreshToken);
        }
        return false;
    }

    /**
     * Thu hồi / Xóa Refresh Token khi người dùng Đăng xuất
     */
    public void revokeRefreshToken(Long userId, String tokenId) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + tokenId;
        try {
            redisTemplate.delete(key);
            log.info("Đã xóa Refresh Token trong Redis: {}", key);
        } catch (Exception e) {
            log.warn("Lỗi khi xóa trong Redis: {}", e.getMessage());
        }
        fallbackStore.remove(key);
    }

    /**
     * Đưa Access Token vào danh sách đen (Blacklist) trong Redis khi Đăng xuất
     * Token sẽ tự động biến mất khi thời gian hết hạn (tối đa 15 phút) trôi qua
     */
    public void blacklistAccessToken(String accessToken, long remainingMs) {
        if (remainingMs <= 0) {
            return;
        }
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        try {
            redisTemplate.opsForValue().set(key, "REVOKED", remainingMs, TimeUnit.MILLISECONDS);
            log.info("Đã đưa Access Token vào Redis Blacklist trong {} ms", remainingMs);
        } catch (Exception e) {
            log.warn("Không kết nối được Redis, lưu Blacklist vào bộ nhớ dự phòng: {}", e.getMessage());
            fallbackStore.put(key, new TokenEntry("REVOKED", remainingMs));
        }
    }

    /**
     * Kiểm tra xem Access Token có đang bị Blacklist không
     */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }
        } catch (Exception e) {
            log.debug("Kiểm tra Redis thất bại, tra cứu fallback: {}", e.getMessage());
        }

        TokenEntry entry = fallbackStore.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                fallbackStore.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }
}
