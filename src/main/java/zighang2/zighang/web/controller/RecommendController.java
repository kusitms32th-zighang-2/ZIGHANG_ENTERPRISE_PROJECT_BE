package zighang2.zighang.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zighang2.zighang.web.dto.JobRecommendDto;
import zighang2.zighang.web.service.RecommendService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/top6")
    @Operation(summary = "추천공고 6개 조회 API", description = "사용자가 회원가입 후, 결과페이지에서 보여지는 추천 공고 6개를 조회하는 API입니다.")
    public List<JobRecommendDto.JobRecommendResponseDto> get6RecommendPosting(){
        return recommendService.recommend6Posting();
    }
}
