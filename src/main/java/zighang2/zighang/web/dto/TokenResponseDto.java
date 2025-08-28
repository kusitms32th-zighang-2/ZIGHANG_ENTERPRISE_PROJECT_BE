package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {

    @Getter
    @AllArgsConstructor
    public static class LoginTokenResponseDto {
        private Long userId;
        private String accessToken;
        private String refreshToken;
    }

    @Getter
    @AllArgsConstructor
    public static class RefreshTokenResponseDto {
        private Long userId;
        private String accessToken;
        private String refreshToken;

        public static RefreshTokenResponseDto of(Long userId, String accessToken,String refreshToken) {
            return new RefreshTokenResponseDto(userId, accessToken,refreshToken);
        }
    }
}
