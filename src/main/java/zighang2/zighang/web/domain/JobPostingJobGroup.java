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
        name = "job_posting_job_group",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_group_id", "job_recommend_id"})
        }
)
public class JobPostingJobGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_group_id")
    private JobGroup jobGroup;

    @ManyToOne
    @JoinColumn(name = "job_recommend_id")
    private JobRecommend jobRecommend;





}
