package zighang2.zighang.global.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.token.refresh-expiration-time}")
    private long refreshExpireSeconds;

    private static final String PREFIX = "REFRESH_TOKEN:";

    public void setRefreshToken(Long userId, String refreshToken) {
        String key = getKey(userId);
        redisTemplate.opsForValue().set(key, refreshToken, refreshExpireSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(Long userId) {
        String key = getKey(userId);
        return redisTemplate.opsForValue().get(key);
    }

    public boolean checkExistsValue(String key) {
        return redisTemplate.hasKey(key);
    }

    public void deleteRefreshToken(Long userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    private String getKey(Long userId) {
        return PREFIX + userId;
    }
}
