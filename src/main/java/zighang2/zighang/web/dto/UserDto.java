package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserRole;
    import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class UserDto {
    private Long id;
    private String email;
    private String nickname;
    private UserRole role;


    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserModifyDto {
        private UserRole userRole;
        private String jobGroup;
        private String companySize;
        private String education;
        private String workExperience;
        private String receivingEmail;

        public UserModifyDto of(User user) {
            return UserModifyDto.builder()
                    .companySize(user.getCompanySize())
                    .jobGroup(user.getJobGroup())
                    .education(user.getEducation())
                    .workExperience(user.getWorkExperience())
                    .receivingEmail(user.getReceivingEmail())
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
