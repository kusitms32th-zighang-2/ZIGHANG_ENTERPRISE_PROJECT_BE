package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.*;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.enums.*;
import zighang2.zighang.web.domain.user.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class JobRecommend extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_recommend_id")
    private Long id;

    @Column(length = 100)
    private String title;

    @Column(length = 50)
    private String companyName;

    @Column(length = 254)
    private String recruitmentAddress;

    @Column(columnDefinition = "TEXT")
    private String ocrData;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private CompanyTypeEnum companyType;

    @Enumerated(EnumType.STRING)
    private Education education;

    @Column(length = 50)
    private String workExperience;

    @Column(length = 50)
    private String welfare;

    private Integer commuteMinutes;

    @OneToMany(mappedBy = "jobRecommend",cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobPostingJobPosition> jobPostingJobPositions = new ArrayList<>();

    @OneToMany(mappedBy = "jobRecommend",cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobPostingJobGroup> jobPostingJobGroups = new ArrayList<>();

    @OneToMany(mappedBy = "jobRecommend",cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobPostingRecruitmentType> jobPostingRecruitmentTypes = new ArrayList<>();

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

    public void updateWorkExperience(String workExperience) {
        this.workExperience = workExperience;
    }
}
