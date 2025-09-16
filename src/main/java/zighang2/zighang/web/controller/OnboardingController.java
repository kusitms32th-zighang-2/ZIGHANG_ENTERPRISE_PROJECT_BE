package zighang2.zighang.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.web.dto.OnboardingDto;
import zighang2.zighang.web.dto.UserDto;
import zighang2.zighang.web.service.OnboardingService;

@RestController
@RequestMapping("/onboardings")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "테스트 캐릭터 발급 API", description = "온보딩 결과를 통해, 온보딩 캐릭터와 기업 규모, 복지 데이터를 제공하는 API입니다.")
    @PostMapping("/character")
    public ApiResponse<OnboardingDto.OnboardingResponse> getOnboardingCharacter(@RequestBody OnboardingDto.OnboardingRequest request) {
        return ApiResponse.onSuccess(onboardingService.getCharacter(request));
    }

    @Operation(summary = "온보딩한 유저의 테스트 캐릭터 발급 API", description = "온보딩한 유저의 테스트 결과를 제공하는 API입니다.")
    @PostMapping("/afterOnboading/character")
    public ApiResponse<OnboardingDto.OnboardingSignupResponse> getCharacterAfterOnboarding(@RequestBody OnboardingDto.OnboardingRequest request) {
        return ApiResponse.onSuccess(onboardingService.getCharacterAfterOnboarding(request));
    }

    @Operation(summary = "테스트 이후 회원가입 API", description = "테스트 이후, 유저의 회원가입을 하는 API입니다.")
    @PostMapping("/test/signup")
    public ApiResponse<OnboardingDto.OnboardingSignupResponse> OnboardingSignup(@RequestBody OnboardingDto.OnboardingSignupRequest request) {
        return ApiResponse.onSuccess(onboardingService.onboardingSignupAfterTest(request));

    }

    @Operation(summary = "테스트없이 회원가입 API", description = "테스트 없이, 유저의 회원가입을 하는 API입니다.")
    @PostMapping("/signup")
    public ApiResponse<UserDto.MypageResponseDto> OnboardingSignup(@RequestBody UserDto.MypageRequestDto request) {
        return ApiResponse.onSuccess(onboardingService.onboardingSignup(request));
    }

}
