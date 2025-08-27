package zighang2.zighang.web.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.RedisService;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.utils.KakaoUtil;
import zighang2.zighang.web.converter.AuthConverter;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.KakaoDTO;
import zighang2.zighang.web.dto.TokenResponse;
import zighang2.zighang.web.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse oAuthLogin(String accessCode, HttpServletResponse httpServletResponse){
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        String email = kakaoProfile.getKakao_account().getEmail();
        String nickname = kakaoProfile.getProperties().getNickname();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(kakaoProfile));

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken();
        redisService.setRefreshToken(user.getEmail(),refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    private User createNewUser(KakaoDTO.KakaoProfile kakaoProfile) {
        User newUser = AuthConverter.toUser(
                kakaoProfile.getKakao_account().getEmail(),
                kakaoProfile.getKakao_account().getProfile().getNickname(),
                null,
                passwordEncoder
        );
        return userRepository.save(newUser);
    }


}
