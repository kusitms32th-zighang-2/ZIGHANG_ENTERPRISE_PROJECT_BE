package zighang2.zighang.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.web.domain.enums.*;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OnboardingDto {

    @Getter
    @AllArgsConstructor
    public static class OnboardingRequest {
        @Schema(description = "기업 유형1", example = "MAJOR")
        private CompanyTypeEnum q1;
        @Schema(description = "기업 유형2", example = "MID_SIZE")
        private CompanyTypeEnum q2;
        @Schema(description = "기업 유형3", example = "STARTUP")
        private CompanyTypeEnum q3;
        @Schema(description = "복지1", example = "휴가")
        private String q4;
        @Schema(description = "복지2", example = "커리어")
        private String q5;
        @Schema(description = "복지3", example = "식대")
        private String q6;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class OnboardingResponse {
        private List<CompanyTypeEnum> companyTypeEnumList;
        private Map<CompanyTypeEnum, Double> companyRatio;
        private List<String> welfareList;
        private Long characterId;
        private String characterName;
    }

    @Getter
    @AllArgsConstructor
    public static class OnboardingSignupRequest {
        private Integer workExperience;
        private Education education;
        private JobGroupEnum jobGroups;
        private List<JobPositionEnum> jobPositions;
        private Integer maxCommuteMinutes;
        private Transport transport;
        private String address;

        // ==== 온보딩 정보 ====
        private Long characterId;
        private List<CompanyTypeEnum> companyList;

        private List<String> welfareList;
        private Map<CompanyTypeEnum, Double> companyRatio;

    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class OnboardingSignupResponse {
        // 온보딩 캐릭터 정보
        private Long characterId;
        private String characterName;

        // 공고데이터
        private List<SearchDto.SearchResponse> jobRecommends;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReOnboardingResponse {
        private Long userId;
        private String name;
        private String message;
    }


}
