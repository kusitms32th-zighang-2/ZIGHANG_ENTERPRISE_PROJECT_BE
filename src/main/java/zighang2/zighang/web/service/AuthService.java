package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public TokenResponseDto oAuthLogin(String accessCode){
        KakaoDto.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
        KakaoDto.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        String email = kakaoProfile.getKakao_account().getEmail();
        String nickname = kakaoProfile.getProperties().getNickname();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, nickname));

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken();
        redisService.setRefreshToken(user.getEmail(),refreshToken);

        return new TokenResponseDto(accessToken, refreshToken);
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
}
