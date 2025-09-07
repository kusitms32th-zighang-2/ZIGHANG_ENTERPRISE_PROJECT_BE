package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class OnboardingCharacter extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    @Column(name = "character_name", length = 20)
    private String characterName;

    @Column(name = "company_type", length = 20)
    private String companyType;

    @Column(length = 20)
    private String welfare;

    @OneToMany(mappedBy = "onboardingCharacter",cascade = CascadeType.ALL)
    private List<User> userList = new ArrayList<>();


}
