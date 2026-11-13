package com.hr.employee_management.Security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private final Map<String, Long> localFallbackBlacklist = new ConcurrentHashMap<>();

    public void revokeToken(String token, long remainingValidityMs) {
        if (token == null || token.isBlank()) {
            return;
        }
        long ttlMs = Math.max(remainingValidityMs, 1000L);
        String redisKey = keyOf(token);

        try {
            stringRedisTemplate.opsForValue().set(redisKey, "revoked", Duration.ofMillis(ttlMs));
            log.info("Token blacklisted in Redis with TTL {} ms", ttlMs);
        } catch (Exception e) {
            log.warn("Redis unavailable for blacklisting token, falling back to local memory: {}", e.getMessage());
            localFallbackBlacklist.put(redisKey, System.currentTimeMillis() + ttlMs);
        }
    }

    public boolean isTokenRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String redisKey = keyOf(token);

        try {
            Boolean hasKey = stringRedisTemplate.hasKey(redisKey);
            return Boolean.TRUE.equals(hasKey) || isInLocal(redisKey);
        } catch (Exception e) {
            log.warn("Redis check failed, falling back to local memory: {}", e.getMessage());
            return isInLocal(redisKey);
        }
    }

    private boolean isInLocal(String redisKey) {
        Long expiresAt = localFallbackBlacklist.get(redisKey);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt <= System.currentTimeMillis()) {
            localFallbackBlacklist.remove(redisKey);
            return false;
        }
        return true;
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        localFallbackBlacklist.entrySet().removeIf(e -> e.getValue() <= now);
    }

    private String keyOf(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return BLACKLIST_KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
