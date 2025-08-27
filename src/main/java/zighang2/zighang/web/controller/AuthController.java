package zighang2.zighang.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.TokenResponse;
import zighang2.zighang.web.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/auth/login/kakao")
    public ApiResponse<TokenResponse> kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse httpServletResponse) {
        return ApiResponse.onSuccess(authService.oAuthLogin(accessCode, httpServletResponse));
    }

}
