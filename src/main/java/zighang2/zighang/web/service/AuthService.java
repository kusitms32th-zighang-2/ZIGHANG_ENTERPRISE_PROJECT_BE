package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.global.service.RedisService;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.auth.jwt.KakaoUtil;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserRole;
import zighang2.zighang.web.dto.KakaoDto;
import zighang2.zighang.web.dto.TokenResponseDto;
import zighang2.zighang.web.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    public TokenResponseDto.LoginTokenResponseDto oAuthLogin(String accessCode){
        KakaoDto.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
        KakaoDto.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        String email = kakaoProfile.getKakao_account().getEmail();
        String nickname = kakaoProfile.getProperties().getNickname();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, nickname));

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);
        redisService.setRefreshToken(user.getEmail(),refreshToken);

        return new TokenResponseDto.LoginTokenResponseDto(user.getId(),accessToken, refreshToken);
    }

    private User createNewUser(String email, String nickname) {
        String rawPassword = UUID.randomUUID().toString();
        return userRepository.save(
                User.builder()
                        .email(email)
                        .name(nickname)
                        .nickname(nickname)
                        .password(passwordEncoder.encode(rawPassword))
                        .userRole(UserRole.GENERAL)
                        .build()
        );
    }

    public TokenResponseDto.RefreshTokenResponseDto recreateAccessToken(String refreshToken) {
        String token = refreshToken.substring(7);

        if (!jwtProvider.validateToken(token,"refresh")) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        String email = jwtProvider.getEmailFromToken(token);
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        String savedToken = redisService.getRefreshToken(email);

        if (savedToken == null || !savedToken.equals(token)) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        return jwtProvider.recreate(user,token);
    }
}
