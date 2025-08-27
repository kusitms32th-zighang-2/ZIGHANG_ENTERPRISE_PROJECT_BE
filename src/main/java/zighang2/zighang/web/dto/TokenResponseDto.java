package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
    @Builder
    @AllArgsConstructor
    public static class RefreshTokenResponseDto {
        private Long userId;
        private String accessToken;

        public static RefreshTokenResponseDto of(Long userId, String accessToken) {
            return new RefreshTokenResponseDto(userId, accessToken);
        }
    }
}
