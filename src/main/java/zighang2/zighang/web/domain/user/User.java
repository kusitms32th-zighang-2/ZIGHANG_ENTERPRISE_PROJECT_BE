package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.*;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.enums.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(length = 50)
    private String address;

    @Enumerated(EnumType.STRING)
    private Transport transport;

    private Integer maxCommuteMinutes;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    private JobGroup jobGroup;

    @Enumerated(EnumType.STRING)
    private JobPosition jobPosition;

    @Column(length = 50)
    private CompanyType companyType;

    @Column(length = 50)
    private Education education;

    @Column(length = 50)
    private String workExperience;

    @Column(length = 50)
    private String receivingEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private OnboardingCharacter onboardingCharacter;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<JobRecommend> jobRecommendList= new ArrayList<>();

    public void updateUsersInfo(JobGroup jobGroup, JobPosition jobPosition, CompanyType companyType, Education education, String workExperience, String receivingEmail) {
        if (jobGroup != null) this.jobGroup = jobGroup;
        if (jobPosition != null) this.jobPosition = jobPosition;
        if (companyType != null) this.companyType = companyType;
        if (education != null) this.education = education;
        if (workExperience != null) this.workExperience = workExperience.trim();
        if (receivingEmail != null) this.receivingEmail = receivingEmail.trim();
    }

}
