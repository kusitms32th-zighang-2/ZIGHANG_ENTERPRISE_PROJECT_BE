package zighang2.zighang.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.web.domain.JobRecommend;

import java.util.List;

public class JobPostingResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPostingDetailDto {
        @Schema(description = "공고 ID", example = "1")
        private Long jobPostingId;
        @Schema(description = "공고 제목", example = "백엔드 개발자 구인")
        private String jobPostingTitle;
        @Schema(description = "회사명", example = "직행")
        private String companyName;
        @Schema(description = "경력",example = "1")
        private Integer workExperience;
        @Schema(description = "학력", example = "학력 무관")
        private String education;
        @Schema(description = "직무", example = "[프론트엔드, 서버_백엔드]")
        private List<String> jobPositions;
        @Schema(description = "근무 형태", example = "전환형 인턴")
        private String recruitmentType;
        @Schema(description = "주소", example = "서울특별시 00구 00대로 123")
        private String recruitmentAddress;
        @Schema(description = "공고 이미지")
        private String content;

        public static JobPostingDetailDto of(JobRecommend jobRecommend){
            return JobPostingDetailDto.builder()
                    .jobPostingId(jobRecommend.getId())
                    .jobPostingTitle(jobRecommend.getTitle())
                    .companyName(jobRecommend.getCompanyName())
                    .education(jobRecommend.getEducation().getDisplayName())
                    .workExperience(jobRecommend.getWorkExperience())
                    .jobPositions(jobRecommend.getJobPostingJobPositions().stream()
                            .map(jp->jp.getJobPosition().getJobPositionName().getDisplay())
                            .toList()
                    )
                    .recruitmentType(jobRecommend.getRecruitmentType().getDisplayName())
                    .recruitmentAddress(jobRecommend.getRecruitmentAddress())
                    .content(jobRecommend.getContent())
                    .build();
        }
    }
}
