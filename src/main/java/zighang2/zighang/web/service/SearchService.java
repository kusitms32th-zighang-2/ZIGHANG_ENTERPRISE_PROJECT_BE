package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.config.RedisConfig;
import zighang2.zighang.global.service.RedisService;
import zighang2.zighang.web.dto.SearchDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RedisService redisService;

//    public List<SearchDto.SearchResponse> recommendJobPostings(SearchDto.SearchRequest request) {
//
//    }
}
