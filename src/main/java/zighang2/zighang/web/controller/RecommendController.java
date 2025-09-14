package zighang2.zighang.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.web.dto.JobPostingResponseDto;
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
    public List<JobRecommendDto.JobRecommendResponseDto> get6Recommends(){
        return recommendService.get6Recommends();
    }

    @GetMapping("/job-postings")
    @Operation(summary = "추천공고 리스트 조회 API", description = "사용자가 추천 공고 리스트 페이지에서 보여지는 추천 공고 전체를 조회하는 API입니다.")
    public ApiResponse<JobPostingResponseDto.JobPostingListDto> getJobPostings(){
        return ApiResponse.onSuccess(recommendService.getJobPostings());
    }

    @GetMapping("/{jobPostingId}")
    @Operation(summary = "공고 상세 조회 API")
    public JobPostingResponseDto.JobPostingDetailDto getJobPostingDetail(@PathVariable Long jobPostingId){
        return recommendService.getJobPostingDetail(jobPostingId);
    }
}
