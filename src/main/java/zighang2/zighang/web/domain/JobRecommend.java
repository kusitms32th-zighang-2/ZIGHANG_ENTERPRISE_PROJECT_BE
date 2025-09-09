package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.enums.*;
import zighang2.zighang.web.domain.user.User;

import java.util.ArrayList;
import java.util.List;

@Getter
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

    @Column(length = 50)
    private String content;

    @Enumerated(EnumType.STRING)
    private CompanyTypeEnum companyType;

    @Enumerated(EnumType.STRING)
    private Education education;

    @Enumerated(EnumType.STRING)
    private RecruitmentType recruitmentType;

    @OneToMany(mappedBy = "jobRecommend")
    private List<JobPostingJobPosition> jobPostingJobPositions = new ArrayList<>();

    @OneToMany(mappedBy = "jobRecommend")
    private List<JobPostingJobGroup> jobPostingJobGroups = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
