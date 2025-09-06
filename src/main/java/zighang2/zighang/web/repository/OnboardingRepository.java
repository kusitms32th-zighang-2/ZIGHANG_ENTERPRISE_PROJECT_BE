package zighang2.zighang.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zighang2.zighang.web.domain.user.OnboardingCharacter;

import java.util.Optional;

@Repository
public interface OnboardingRepository extends JpaRepository<OnboardingCharacter, Long> {
    Optional<OnboardingCharacter> findByCompanyTypeAndWelfare(String companyType, String welfare);
}
