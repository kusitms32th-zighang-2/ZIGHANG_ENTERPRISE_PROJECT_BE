package zighang2.zighang.web.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;
import zighang2.zighang.web.domain.user.UserCompanyType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class CompanyType extends BaseEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_type_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type_name")
    private CompanyTypeEnum companyTypeName;

    @OneToMany(mappedBy = "companyType", orphanRemoval=true)
    private List<UserCompanyType> userCompanyTypes = new ArrayList<>();

}
