package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.RecruitmentType;
import zighang2.zighang.web.domain.enums.RecruitmentTypeEnum;

import java.util.Optional;

@Repository
public interface RecruitmentTypeRepository extends JpaRepository<RecruitmentType, Long> {
    Optional<RecruitmentType> findByRecruitmentType(RecruitmentTypeEnum typeEnum);
}
