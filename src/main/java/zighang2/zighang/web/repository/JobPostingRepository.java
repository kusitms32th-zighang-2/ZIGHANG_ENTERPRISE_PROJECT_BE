package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.JobRecommend;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobRecommend, Long> {

    List<JobRecommend> findTop10ByOrderByIdDesc();
}
