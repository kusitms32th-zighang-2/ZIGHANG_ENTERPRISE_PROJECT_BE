package zighang2.zighang.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import zighang2.zighang.global.utils.WorkExperienceFormatter;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.enums.Transport;

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
        @Schema(description = "경력")
        private String workExperience;
        @Schema(description = "경력")
        private String welfare;
        @Schema(description = "학력", example = "학력 무관")
        private String education;
        @Schema(description = "직무", example = "'['프론트엔드', '서버_백엔드']'")
        private List<String> jobPositions;
        @Schema(description = "근무 형태", example = "'['전환형인턴', '정규직']'")
        private List<String> recruitmentType;
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
                    .welfare(jobRecommend.getWelfare())
                    .workExperience(jobRecommend.getWorkExperience())
                    .jobPositions(jobRecommend.getJobPostingJobPositions().stream()
                            .map(jp->jp.getJobPosition().getJobPositionName().getDisplay())
                            .toList()
                    )
                    .recruitmentType(jobRecommend.getJobPostingRecruitmentTypes().stream()
                            .map(jr->jr.getRecruitmentType().getRecruitmentType().getDisplayName())
                            .toList()
                    )
                    .recruitmentAddress(jobRecommend.getRecruitmentAddress())
                    .content(jobRecommend.getContent())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPostingListDto {
        private Long jobPostingId;
        private String companyName;
        private String jobPostingTitle;
        private String workExperience;
        private List<String> recruitmentType;
        private String education;
        private Integer commuteMinutes;
        private Transport transport;
        private String welfare;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPostingListWrapper {
        private List<JobPostingListDto> jobs;
        private boolean hasNext;
    }
}
