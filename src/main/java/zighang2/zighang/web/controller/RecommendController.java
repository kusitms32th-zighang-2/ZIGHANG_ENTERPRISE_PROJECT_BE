package zighang2.zighang.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import zighang2.zighang.web.dto.JobPostingDto;
import zighang2.zighang.web.service.RecommendService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/top6")
    public List<JobPostingDto.JobPostingResponseDto> get6RecommendPosting(){
        return recommendService.recommend6Posting();
    }
}
