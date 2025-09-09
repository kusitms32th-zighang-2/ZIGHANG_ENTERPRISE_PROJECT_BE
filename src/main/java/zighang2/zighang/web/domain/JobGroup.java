package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.enums.JobGroupEnum;
import zighang2.zighang.web.domain.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "job_groups")
@Getter
public class JobGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_group_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_group_name")
    private JobGroupEnum jobGroupName;

    @OneToMany(mappedBy = "jobGroup")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "jobGroup")
    private List<JobPosition> jobPositions = new ArrayList<>();

    @OneToMany(mappedBy = "jobGroup")
    private List<JobPostingJobGroup> jobPostingJobGroups = new ArrayList<>();
}