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
import zighang2.zighang.web.domain.user.enums.UserRole;
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

    public TokenResponseDto.LoginTokenResponseDto oAuthLogin(String authorizationCode){
        KakaoDto.OAuthToken oAuthToken = kakaoUtil.requestToken(authorizationCode);
        KakaoDto.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        String email = kakaoProfile.getKakao_account().getEmail();
        String name = kakaoProfile.getProperties().getNickname();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, name));

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);
        redisService.setRefreshToken(user.getId(), refreshToken);

        return new TokenResponseDto.LoginTokenResponseDto(user.getId(),accessToken, refreshToken);
    }

    private User createNewUser(String email, String name) {
        String rawPassword = UUID.randomUUID().toString();
        String uniqueNickname = UUID.randomUUID().toString().substring(0, 8);

        User user =User.builder()
                .email(email)
                .name(name)
                .nickname(uniqueNickname)
                .password(passwordEncoder.encode(rawPassword))
                .userRole(UserRole.GENERAL)
                .build();

        return userRepository.save(user);
    }

    public TokenResponseDto.RefreshTokenResponseDto recreateAccessToken(String refreshToken) {
        String token = refreshToken.substring(7);

        if (!jwtProvider.validateToken(token,"refresh")) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        String savedToken = redisService.getRefreshToken(user.getId());

        if (savedToken == null || !savedToken.equals(token)) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        return jwtProvider.recreate(user,token);
    }

    public void logoutUser(String token){
        Long userId = jwtProvider.getCurrentUserId();

        redisService.addToBlackList(token,"logout");
        redisService.deleteRefreshToken(userId);
    }
}
