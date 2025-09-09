package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.CompanyType;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Table(name = "user_company_type",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_type_id", "user_id"})
        })
public class UserCompanyType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_company_type_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_type_id")
    private CompanyType companyType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
