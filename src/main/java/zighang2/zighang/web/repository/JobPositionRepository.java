package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.JobGroup;
import zighang2.zighang.web.domain.JobPosition;
import zighang2.zighang.web.domain.enums.JobPositionEnum;

import java.util.Optional;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    Optional<JobPosition> findByJobPositionNameAndJobGroup(JobPositionEnum jobPositionEnum, JobGroup jobGroup);
}
