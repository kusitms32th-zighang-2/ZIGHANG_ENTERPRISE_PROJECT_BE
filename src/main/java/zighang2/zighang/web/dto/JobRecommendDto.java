package zighang2.zighang.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.web.domain.enums.CompanyType;
import zighang2.zighang.web.domain.JobRecommend;

public class JobRecommendDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobRecommendResponseDto {
        @Schema(description = "공고 ID", example = "1")
        private Long postingId;
        @Schema(description = "공고 제목", example = "백엔드 개발자 구인")
        private String postingTitle;
        @Schema(description = "회사명", example = "직행")
        private String company;
        @Schema(description = "기업 유형", example = "STARTUP")
        private CompanyType companyType;
        @Schema(description = "채용 주소", example = "서울특별시 00구 00대로 123")
        private String address;

        public static JobRecommendResponseDto of(JobRecommend jobRecommend) {
            return new JobRecommendResponseDto(jobRecommend.getId(), jobRecommend.getTitle(), jobRecommend.getCompanyName(), jobRecommend.getCompanyType(), jobRecommend.getRecruitmentAddress());
        }
    }

}
