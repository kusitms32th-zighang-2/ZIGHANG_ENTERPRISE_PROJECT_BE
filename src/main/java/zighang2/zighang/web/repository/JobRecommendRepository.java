package zighang2.zighang.web.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.user.User;

import java.util.List;

@Repository
public interface JobRecommendRepository extends JpaRepository<JobRecommend, Long> {

    List<JobRecommend> findTop10ByOrderByIdDesc();

    // user별, id 역순, lastId 이후 데이터 페이징
    List<JobRecommend> findTop10ByUserAndIdLessThanOrderByIdDesc(User user, Long lastId);

    // 첫 로딩용
    List<JobRecommend> findTop10ByUserOrderByIdDesc(User user);

    // user 데이터로 모두 지우기
    void deleteAllByUser(User user);
}
