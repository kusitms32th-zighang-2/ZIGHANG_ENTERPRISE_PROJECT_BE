package zighang2.zighang.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zighang2.zighang.web.service.SearchService;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

//    @PostMapping
//    public ApiResponse<List<SearchDto.SearchResponse>> search(@RequestBody SearchDto.SearchRequest request) {
//        return ApiResponse.onSuccess(searchService.recommendJobPostings(request));
//    }
}
