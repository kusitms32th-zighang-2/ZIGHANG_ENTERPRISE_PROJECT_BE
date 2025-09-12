package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.service.RedisService;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RedisService redisService;

//    public List<SearchDto.SearchResponse> recommendJobPostings(SearchDto.SearchRequest request) {
//
//    }
}
