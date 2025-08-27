package zighang2.zighang.global;

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

    public void setRefreshToken(String key, String refreshToken) {
        redisTemplate.opsForValue()
                .set(key, refreshToken, refreshExpireSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String key) {
        redisTemplate.delete(key);
    }
}
