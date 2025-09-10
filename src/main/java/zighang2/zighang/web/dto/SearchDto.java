package zighang2.zighang.web.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import zighang2.zighang.web.domain.enums.*;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class SearchDto {

    public static class SearchRequest {
        // ==== 필터링 정보 ====
        private String workExperience; // 경력
        private Education education; // 학력
        private JobGroupEnum jobGroupEnum; // 직군
        private JobPositionEnum jobPositionEnum; // 직무

        //
        private Integer maxCommuteMinutes;
        private Transport transport;
        private String address;

        // ==== 온보딩 정보 ====
        private List<CompanyTypeEnum> companyTypeEnumList;
        private List<String> welfareList; // redis
        private Map<String, Double> companyRatio;

    }

    public static class SearchResponse {

    }
}
