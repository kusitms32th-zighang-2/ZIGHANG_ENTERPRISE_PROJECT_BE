package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import zighang2.zighang.web.domain.enums.Education;
import zighang2.zighang.web.domain.enums.JobGroup;
import zighang2.zighang.web.domain.enums.JobPosition;
import zighang2.zighang.web.domain.enums.Transport;

import java.sql.Time;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OnboardingDto {

    @Getter
    @AllArgsConstructor
    public static class OnboardingRequest {
        private String q1;
        private String q2;
        private String q3;
        private String q4;
        private String q5;
        private String q6;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class OnboardingResponse {
        private String companyTypeFinal;
        private Map<String, Double> companyRatio;
        private List<String> welfareList;
        private Long characterId;
        private String characterName;
    }

    @Getter
    @AllArgsConstructor
    public static class OnboardingSignupRequest {
        private String workExperience;
        private Education education;
        private JobGroup jobGroup;
        private JobPosition jobPosition;
        private Time maxCommuteMinutes;
        private Transport transport;
        private String address;

        // ==== 온보딩 정보 ====
        private Long characterId;
        private String companyTypeFinal;
        private List<String> welfareList; // redis
        private Map<String, Double> companyRatio; // redis

    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class OnboardingSignupResponse {

    }

}
