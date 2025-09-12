package zighang2.zighang.web.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import zighang2.zighang.web.domain.JobRecommend;
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

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SearchResponse {
        private String title; // 공고 제목
        private String companyName; //회사이름
        private String companyAddress; //회사 주소
        private List<String> jobPositions;
        private List<String> jobGroup;
        private String companyType;
        private Integer commuteTimeMinutes; // 정밀 계산 값

        public static SearchDto.SearchResponse of(JobRecommend jobRec, Integer commuteTimeMinutes) {
            return SearchDto.SearchResponse.builder()
                    .title(jobRec.getTitle())
                    .companyName(jobRec.getCompanyName())
                    .companyAddress(jobRec.getRecruitmentAddress())
                    .jobGroup(jobRec.getJobPostingJobGroups().stream()
                            .map(jg->jg.getJobGroup().getJobGroupName().getDisplay())
                            .toList())
                    .jobPositions(jobRec.getJobPostingJobPositions().stream()
                            .map(jp -> jp.getJobPosition().getJobPositionName().getDisplay())
                            .toList())
                    .companyType(jobRec.getCompanyType().getDisplay())
                    .commuteTimeMinutes(commuteTimeMinutes)
                    .build();
        }

    }
}
