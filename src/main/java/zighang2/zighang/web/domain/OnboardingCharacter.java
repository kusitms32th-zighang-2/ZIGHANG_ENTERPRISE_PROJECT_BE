package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.enums.CharacterName;
import zighang2.zighang.web.domain.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class OnboardingCharacter extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private CharacterName characterName;

    @Column(name = "company_type", length = 20)
    private String companyType;

    @Column(length = 20)
    private String welfare;

    @OneToMany(mappedBy = "onboardingCharacter",cascade = CascadeType.ALL)
    private List<User> userList = new ArrayList<>();


}
