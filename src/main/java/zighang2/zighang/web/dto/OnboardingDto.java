package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import zighang2.zighang.web.domain.enums.JobGroup;
import zighang2.zighang.web.domain.enums.JobPosition;

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
        private String characterName;
    }

    @Getter
    @AllArgsConstructor
    public static class OnboardingSignupRequest {
        private String workExperience;
        private String education;
        private JobGroup jobGroup;
        private JobPosition jobPosition;


    }

//    @Getter
//    @AllArgsConstructor
//    @Builder
//    public static class OnboardingSignupResponse {
//
//    }

}
