package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.CompanyType;

@Repository
public interface CompanyTypeRepository extends JpaRepository<CompanyType, Long> {
}
