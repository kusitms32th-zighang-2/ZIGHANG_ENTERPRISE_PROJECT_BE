package zighang2.zighang.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;
import zighang2.zighang.web.dto.KakaoDto;
import zighang2.zighang.web.dto.TokenResponseDto;
import zighang2.zighang.web.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/kakao")
    public ApiResponse<TokenResponseDto.LoginTokenResponseDto> kakaoLogin(@RequestParam("code") String accessCode) {
        return ApiResponse.onSuccess(authService.oAuthLogin(accessCode));
    }

    @GetMapping("/refresh-token")
    @Operation(summary = "JWT 토큰 재발급 API")
    public ApiResponse<TokenResponseDto.RefreshTokenResponseDto> refreshToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new GeneralException(ErrorStatus.TOKEN_NOT_FOUND);
        }
        return ApiResponse.onSuccess(authService.recreateAccessToken(token));
    }

}
