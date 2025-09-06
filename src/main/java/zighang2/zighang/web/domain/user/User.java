package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.*;
import zighang2.zighang.global.common.BaseEntity;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(length = 50)
    private String jobGroup;

    @Column(length = 50)
    private String companySize;

    @Column(length = 50)
    private String education;

    @Column(length = 50)
    private String workExperience;

    @Column(length = 50)
    private String receivingEmail;

    @OneToMany(fetch = FetchType.LAZY)
    private List<JobGroup> jobs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private OnboardingCharacter character;

    public void updateUsersInfo(String jobGroup, String companySize, String eduation, String workExperience, String receivingEmail) {
        if (jobGroup != null) this.jobGroup = jobGroup.trim();
        if (companySize != null) this.companySize = companySize.trim();
        if (eduation != null) this.education = eduation.trim();
        if (workExperience != null) this.workExperience = workExperience.trim();
        if (receivingEmail != null) this.receivingEmail = receivingEmail.trim();
    }

}
