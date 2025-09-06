package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zighang2.zighang.web.domain.CompanyType;
import zighang2.zighang.web.domain.JobRecommend;

public class JobRecommendDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobRecommendResponseDto {
        Long postingId;
        String postingTitle;
        String company;
        CompanyType companyType;
        String address;

        public static JobRecommendResponseDto of(JobRecommend jobRecommend) {
            return new JobRecommendResponseDto(jobRecommend.getId(), jobRecommend.getTitle(), jobRecommend.getCompanyName(), jobRecommend.getCompanyType(), jobRecommend.getRecruitmentAddress());
        }
    }

}
