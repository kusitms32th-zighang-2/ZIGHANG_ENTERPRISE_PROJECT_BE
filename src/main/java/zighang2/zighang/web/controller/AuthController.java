package zighang2.zighang.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;
import zighang2.zighang.web.dto.TokenResponseDto;
import zighang2.zighang.web.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 로그인", description = "인가 코드를 통해 토큰을 발급받는 카카오 로그인 API입니다.")
    public ApiResponse<TokenResponseDto.LoginTokenResponseDto> kakaoLogin(@RequestParam("code") String accessCode) {
        return ApiResponse.onSuccess(authService.oAuthLogin(accessCode));
    }

    @GetMapping("/refresh-token")
    @Operation(summary = "토큰 재발급 API", description = "accessToken이 만료된 경우, refreshToken을 통해 재발급 받는 API입니다")
    public ApiResponse<TokenResponseDto.RefreshTokenResponseDto> refreshToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new GeneralException(ErrorStatus.TOKEN_NOT_FOUND);
        }
        return ApiResponse.onSuccess(authService.recreateAccessToken(token));
    }

    @PostMapping("/sign-out")
    @Operation(summary = "카카오 로그아웃", description = "카카오 로그아웃을 하는 API입니다.")
    public ApiResponse<String> logout(HttpServletRequest request){
        String token = jwtProvider.resolveToken(request);
        log.info("logout token: {}", token);
        if (token == null || token.isEmpty()) {
            throw new GeneralException(ErrorStatus.TOKEN_NOT_FOUND);
        }
        authService.logoutUser(token);
        return ApiResponse.onSuccess("Logout successful");
    }

}
