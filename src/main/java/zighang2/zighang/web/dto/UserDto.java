package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.web.domain.enums.*;
import zighang2.zighang.web.domain.user.User;

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
    public static class MyPageDto {
        private Long id;
        private String email;
        private String name;
        private MypageModifyDto myPageModifyDto;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = PROTECTED)
    public static class MypageModifyDto {
        private JobGroup jobGroup;
        private JobPosition jobPosition;
        private CompanyType companyType;
        private Education education;
        private String workExperience;
        private String receivingEmail;

        public static MypageModifyDto of(User user) {
            return MypageModifyDto.builder()
                    .companyType(user.getCompanyType())
                    .jobGroup(user.getJobGroup())
                    .jobPosition(user.getJobPosition())
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
