package zighang2.zighang.web.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_posting_job_group")
public class JobPostingJobGroup extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "job_position_id")
    private JobGroup jobGroup;

    @ManyToOne
    @JoinColumn(name = "job_recommend_id")
    private JobRecommend jobRecommend;





}
