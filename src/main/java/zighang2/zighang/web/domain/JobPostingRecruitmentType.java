package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;

@Entity
@NoArgsConstructor
@Getter
@Builder
@AllArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "job_posting_recruitment_type",
                columnNames = {"job_recommend_id", "recruitment_type_Id"}
        )
)
public class JobPostingRecruitmentType extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_recommend_id")
    private JobRecommend jobRecommend;

    @ManyToOne
    @JoinColumn(name = "recruitment_type_Id")
    private RecruitmentType recruitmentType;

}
