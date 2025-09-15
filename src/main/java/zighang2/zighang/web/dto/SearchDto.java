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

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SearchResponse {
        private String title; // 공고 제목
        private String companyName; //회사이름
        private String companyAddress; //회사 주소
        private List<String> jobPositions;
        private List<String> jobGroups;
        private String companyType;
        private Integer commuteTimeMinutes; // 정밀 계산 값

        public static SearchDto.SearchResponse of(JobRecommend jobRec, Integer commuteTimeMinutes) {
            return SearchDto.SearchResponse.builder()
                    .title(jobRec.getTitle())
                    .companyName(jobRec.getCompanyName())
                    .companyAddress(jobRec.getRecruitmentAddress())
                    .jobGroups(jobRec.getJobPostingJobGroups() == null ? List.of()
                            : jobRec.getJobPostingJobGroups().stream()
                            .map(jg->jg.getJobGroup().getJobGroupName().getDisplay())
                            .toList())
                    .jobPositions(jobRec.getJobPostingJobPositions() == null ? List.of()
                            : jobRec.getJobPostingJobPositions().stream()
                            .map(jp -> jp.getJobPosition().getJobPositionName().getDisplay())
                            .toList())
                    .companyType(jobRec.getCompanyType() != null ? jobRec.getCompanyType().getDisplay() : null)
                    .commuteTimeMinutes(commuteTimeMinutes)
                    .build();
        }

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SearchResponse_2 {
        private String title; // 공고 제목
        private String companyName; //회사이름

        public static SearchResponse_2 of(JobRecommend jobRec) {
            return SearchResponse_2.builder()
                    .title(jobRec.getTitle())
                    .companyName(jobRec.getCompanyName())
                    .build();
        }
    }
}
