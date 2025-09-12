package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import zighang2.zighang.web.domain.enums.*;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OnboardingDto {

    @Getter
    @AllArgsConstructor
    public static class OnboardingRequest {
        private CompanyTypeEnum q1;
        private CompanyTypeEnum q2;
        private CompanyTypeEnum q3;
        private String q4;
        private String q5;
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
        private JobGroupEnum jobGroupEnum;
        private List<JobPositionEnum> jobPositionEnum;
        private Integer maxCommuteMinutes;
        private Transport transport;
        private String address;

        // ==== 온보딩 정보 ====
        private Long characterId;
        private List<CompanyTypeEnum> companyList;

        private List<String> welfareList;
        private Map<String, Double> companyRatio;

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

}
