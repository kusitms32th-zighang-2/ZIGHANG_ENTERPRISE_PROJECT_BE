package zighang2.zighang.web.dto;

import jakarta.persistence.Column;
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


    @Getter
    @Builder
    @AllArgsConstructor
    public static class userModifyDto {
        private UserRole userRole;
        private String jobGroup;
        private String companySize;
        private String eduation;
        private String workExperience;
        private String receivingEmail;


        public UserDto.userModifyDto of(User user) {
            return userModifyDto.builder()
                    .companySize(user.getCompanySize())
                    .userRole(userRole)
                    .jobGroup(jobGroup)
                    .eduation(eduation)
                    .workExperience(workExperience)
                    .receivingEmail(receivingEmail)
                    .build();

        }
    }

    public static UserDto of(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getUserRole())
                .build();
    }
}
