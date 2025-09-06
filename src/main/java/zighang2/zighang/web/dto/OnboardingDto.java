package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
        private String companyTypeFinal;    // 최종 기업규모
        private Map<String, Double> companyRatio; // 기업규모 비율
        private List<String> welfareList;        // 최종 복지
        private String characterName;       // 매칭된 캐릭터 이름
    }

}
