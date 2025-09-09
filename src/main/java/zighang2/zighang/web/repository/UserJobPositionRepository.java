package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserJobPosition;

import java.util.List;

@Repository
public interface UserJobPositionRepository extends JpaRepository<UserJobPosition, Long> {
    void deleteByUser_Id(Long userId);

    List<UserJobPosition> findByUser_Id(Long id);
}
