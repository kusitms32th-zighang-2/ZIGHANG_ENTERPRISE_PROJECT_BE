package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.enums.CompanyType;
import zighang2.zighang.web.domain.user.OnboardingCharacter;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.OnboardingDto;
import zighang2.zighang.web.repository.OnboardingRepository;
import zighang2.zighang.web.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    public OnboardingDto.OnboardingResponse getOnboardingCharacter(OnboardingDto.OnboardingRequest request) {

        // 1. 기업규모 카운팅 & 비율 계산 로직
        List<CompanyType> companyAnswers = List.of(
                parseCompanyType(request.getQ1()),
                parseCompanyType(request.getQ2()),
                parseCompanyType(request.getQ3())
        );

        Map<CompanyType, Long> companyCount = companyAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans, Collectors.counting()));

        List<CompanyType> companyTypes = Arrays.stream(CompanyType.values())
                .filter(ct -> ct != CompanyType.MIXED)
                .toList();

        Map<CompanyType, Double> companyRatio = new LinkedHashMap<>();
        int baseScore = 1;
        double totalScore = 0.0;

        Map<CompanyType, Integer> scoreMap = new HashMap<>();
        for (CompanyType type : companyTypes) {
            int score = baseScore + companyCount.getOrDefault(type, 0L).intValue();
            scoreMap.put(type, score);
            totalScore += score;
        }

        for (CompanyType type : companyTypes) {
            companyRatio.put(type, scoreMap.get(type) / totalScore);
        }

        CompanyType companyTypeFinal = resolveFinal(companyCount, CompanyType.MIXED);

        // 2. 복지 카운팅
        List<String> welfareAnswers = List.of(request.getQ4(), request.getQ5(), request.getQ6());
        Map<String, Long> welfareCount = welfareAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans, Collectors.counting()));
        String welfareFinal = resolveFinal(welfareCount, "all");

        // 3. DB에서 캐릭터 조회
        OnboardingCharacter character = onboardingRepository.findByCompanyTypeAndWelfare(companyTypeFinal.getDisplayName(), welfareFinal)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHARACTER_NOT_FOUND));


        // 4. 사용자가 선택한 기업 규모, 복지
        List<String> welfareList = welfareAnswers.stream()
                .distinct()
                .toList();

        List<CompanyType> companyTypeList = companyAnswers.stream()
                .distinct()
                .toList();

        // 5. Response DTO 반환
        return OnboardingDto.OnboardingResponse.builder()
                .companyTypeList(companyTypeList)
                .companyRatio(companyRatio)
                .welfareList(welfareList)
                .characterId(character.getId())
                .characterName(character.getCharacterName().getDisplayName())
                .build();
    }

    private <T> T resolveFinal(Map<T, Long> countMap, T fallback) {
        if (countMap.isEmpty()) return null;

        long max = countMap.values().stream()
                .mapToLong(v -> v)
                .max()
                .orElse(0);

        List<T> top = countMap.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .toList();

        return top.size() == 1 ? top.get(0) : fallback;
    }

    private CompanyType parseCompanyType(String input) {
        return Arrays.stream(CompanyType.values())
                .filter(ct -> ct.getDisplayName().equals(input)) // 한글 displayName 매칭
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid company type: " + input));
    }

    public OnboardingDto.OnboardingSignupResponse onboardingSignup(OnboardingDto.OnboardingSignupRequest request) {
        // user 확인
        User user = userRepository.findById(jwtProvider.getCurrentUserId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));
        // 데이터 저장
        User.builder().build();




        // redis에 데이터 저장
        return null;
    }
}
