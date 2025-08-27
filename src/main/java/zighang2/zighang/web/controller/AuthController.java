package zighang2.zighang.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.web.dto.TokenResponseDto;
import zighang2.zighang.web.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/auth/login/kakao")
    public ApiResponse<TokenResponseDto> kakaoLogin(@RequestParam("code") String accessCode) {
        return ApiResponse.onSuccess(authService.oAuthLogin(accessCode));
    }

}
