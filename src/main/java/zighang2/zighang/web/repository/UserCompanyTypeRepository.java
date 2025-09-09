package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.user.UserCompanyType;

import java.util.List;

@Repository
public interface UserCompanyTypeRepository extends JpaRepository<UserCompanyType, Long> {
    List<UserCompanyType> findByUserId(Long id);

}
