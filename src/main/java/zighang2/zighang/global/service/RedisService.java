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
    private long refreshTokenExpirationTime;

    @Value("${jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    private static final String PREFIX = "REFRESH_TOKEN:";

    public void setRefreshToken(Long userId, String refreshToken) {
        String key = getKey(userId);
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpirationTime, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long userId) {
        String key = getKey(userId);
        return redisTemplate.opsForValue().get(key);
    }

    public boolean checkExistsValue(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void deleteRefreshToken(Long userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    private String getKey(Long userId) {
        return PREFIX + userId;
    }

    public boolean isBlackListed(String token){
        return Boolean.TRUE.equals(redisTemplate.hasKey("BLACKLIST:"+token));
    }

    public void addToBlackList(String token, String reason) {
        redisTemplate.opsForValue().set("BLACKLIST:" + token, reason, accessExpirationTime, TimeUnit.MILLISECONDS);
    }
}
