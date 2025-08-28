package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserRole;

@Getter
@Builder
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String nickname;
    private UserRole role;

    public static UserDto of(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getUserRole())
                .build();
    }
}
