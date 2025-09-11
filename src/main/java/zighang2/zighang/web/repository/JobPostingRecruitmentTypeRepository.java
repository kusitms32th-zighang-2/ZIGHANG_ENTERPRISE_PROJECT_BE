package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.JobPostingRecruitmentType;

@Repository
public interface JobPostingRecruitmentTypeRepository extends JpaRepository<JobPostingRecruitmentType, Long> {
}
