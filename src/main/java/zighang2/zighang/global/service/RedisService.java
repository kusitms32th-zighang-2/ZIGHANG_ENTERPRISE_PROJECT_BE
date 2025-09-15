package zighang2.zighang.global.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;

import java.util.List;
import java.util.Map;
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
    private static final String BLACKLIST_PREFIX = "BLACKLIST:";
    private static final String ONBOARDING_RATIO_PREFIX = "ONBOARDING:RATIO:";
    private static final String ONBOARDING_WELFARE_PREFIX = "ONBOARDING:WELFARE:";
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX+token));
    }

    public void addToBlackList(String token, String reason) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, reason, accessExpirationTime, TimeUnit.MILLISECONDS);
    }

    // companyRatio 저장
    public void saveCompanyRatio(Long userId, Map<CompanyTypeEnum, Double> companyRatio) {
        try {
            String key = ONBOARDING_RATIO_PREFIX + userId;
            String value = objectMapper.writeValueAsString(companyRatio);
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            throw new RuntimeException("Redis 저장 실패 (companyRatio)", e);
        }
    }

    // welfareList 저장
    public void saveWelfareList(Long userId, List<String> welfareList) {
        try {
            String key = ONBOARDING_WELFARE_PREFIX + userId;
            String value = objectMapper.writeValueAsString(welfareList);
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            throw new RuntimeException("Redis 저장 실패 (welfareList)", e);
        }
    }

    // companyRatio 불러오기
    public Map<CompanyTypeEnum, Double> getCompanyRatio(Long userId) {
        String value = redisTemplate.opsForValue().get(ONBOARDING_RATIO_PREFIX + userId);
        try {
            return value == null ? null : objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    // welfareList 불러오기
    public List<String> getWelfareList(Long userId) {
        String value = redisTemplate.opsForValue().get(ONBOARDING_WELFARE_PREFIX + userId);
        try {
            return value == null ? null : objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
