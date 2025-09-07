package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.*;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.enums.JobGroup;
import zighang2.zighang.web.domain.enums.JobPosition;
import zighang2.zighang.web.domain.enums.Transport;
import zighang2.zighang.web.domain.enums.UserRole;

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
    private String companySize;

    @Column(length = 50)
    private String education;

    @Column(length = 50)
    private String workExperience;

    @Column(length = 50)
    private String receivingEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private OnboardingCharacter onboardingCharacter;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<JobRecommend> jobRecommendList= new ArrayList<>();

    public void updateUsersInfo(JobGroup jobGroup, String companySize, String eduation, String workExperience, String receivingEmail) {
        if (jobGroup != null) this.jobGroup = jobGroup;
        if (companySize != null) this.companySize = companySize.trim();
        if (eduation != null) this.education = eduation.trim();
        if (workExperience != null) this.workExperience = workExperience.trim();
        if (receivingEmail != null) this.receivingEmail = receivingEmail.trim();
    }

}
