package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.*;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.JobGroup;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.OnboardingCharacter;
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
    private Education education; // 학력

    private Integer workExperience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private OnboardingCharacter onboardingCharacter;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<JobRecommend> jobRecommendList= new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserJobPosition> userJobPositions = new ArrayList<>(); // 직무

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<UserCompanyType> userCompanyTypes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "job_group_id")
    private JobGroup jobGroup; // 직군

    public void updateUsersInfo(
                                Education education,
                                Integer workExperience,
                                String address,
                                Transport transport,
                                Integer maxCommuteMinutes) {
        if (education != null) this.education = education;
        if (workExperience != null) this.workExperience = workExperience;
        if (address != null) this.address = address.trim();
        if (transport != null) this.transport = transport;
        if (maxCommuteMinutes != null) this.maxCommuteMinutes = maxCommuteMinutes;
    }

    public void updateUsersJobGroup(JobGroup jobGroup) {
        this.jobGroup = jobGroup;
    }

    public void updateOnboardingCharacter(OnboardingCharacter onboardingCharacter) {
        this.onboardingCharacter = onboardingCharacter;
    }


    public void updateOnboardingInfo(Education education,
                                     int workExperience,
                                     String address,
                                     Transport transport,
                                     int maxCommuteMinutes,
                                     JobGroup jobGroup) {
        this.education = education;
        this.workExperience = workExperience;
        this.address = address;
        this.transport = transport;
        this.maxCommuteMinutes = maxCommuteMinutes;
        this.jobGroup = jobGroup;
        this.userRole = UserRole.GENERAL; // 신규 가입 시 기본 롤
    }
}
