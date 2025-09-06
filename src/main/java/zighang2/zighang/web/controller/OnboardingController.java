package zighang2.zighang.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.web.dto.OnboardingDto;
import zighang2.zighang.web.service.OnboardingService;

@RestController
@RequestMapping("/onboardings")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/character")
    public ApiResponse<OnboardingDto.OnboardingResponse> getOnboardingCharacter(@RequestBody OnboardingDto.OnboardingRequest request) {
        return ApiResponse.onSuccess(onboardingService.getOnboardingCharacter(request));
    }

}
