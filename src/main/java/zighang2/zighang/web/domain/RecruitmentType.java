package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.enums.RecruitmentTypeEnum;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class RecruitmentType extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_type_Id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private RecruitmentTypeEnum recruitmentType;

    @OneToMany(mappedBy = "recruitmentType")
    private List<JobPostingRecruitmentType> jobPostingRecruitmentTypes = new ArrayList<>();

}
