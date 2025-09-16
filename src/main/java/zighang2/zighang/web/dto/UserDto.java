package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.web.domain.enums.*;
import zighang2.zighang.web.domain.user.User;

import java.util.List;

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
        private Long characterId;
        private String characterName;
        private MypageModifyResponse MypageModifyResponse;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = PROTECTED)
    public static class MyPageAllResponse {
        private Long id;
        private Long characterId;
        private String characterName;

        List<SearchDto.SearchResponse_2> searchResponses;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = PROTECTED)
    public static class MypageModifyRequest {
        private JobGroupEnum jobGroups;
        private List<JobPositionEnum> jobPositions;
        private Education education;
        private Integer workExperience;
        private String address;
        private Transport transport;
        private Integer maxCommuteMinutes;
        private String receivingEmail;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = PROTECTED)
    public static class MypageModifyResponse {
        private String jobGroups;
        private List<String> jobPositions;
        private String education;
        private Integer workExperience;
        private String address;
        private String transport;
        private Integer maxCommuteMinutes;
        private String receivingEmail;

        public static MypageModifyResponse of(User user) {
            return MypageModifyResponse.builder()
                    .jobGroups(user.getJobGroup().getJobGroupName().getDisplay())
                    .jobPositions(
                            user.getUserJobPositions().stream()
                                    .map(up -> up.getJobPosition().getJobPositionName().getDisplay())
                                    .toList()
                    )
                    .education(user.getEducation().getDisplayName())
                    .workExperience(user.getWorkExperience())
                    .address(user.getAddress())
                    .transport(String.valueOf(user.getTransport()))
                    .maxCommuteMinutes(user.getMaxCommuteMinutes())
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
