package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.enums.JobPositionEnum;
import zighang2.zighang.web.domain.user.UserJobPosition;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "job_positions")
@Getter
public class JobPosition extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "job_position_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_position_name")
    private JobPositionEnum jobPositionName;

    @ManyToOne
    @JoinColumn(name = "job_group_id")
    private JobGroup jobGroup;

    @OneToMany(mappedBy = "jobPosition", orphanRemoval=true)
    private List<UserJobPosition> userJobPositions = new ArrayList<>();

    @OneToMany(mappedBy = "jobPosition")
    private List<JobPostingJobPosition> jobPostingJobPositions = new ArrayList<>();


}
