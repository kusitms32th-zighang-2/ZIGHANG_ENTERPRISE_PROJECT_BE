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

        // 1. 기업규모 카운팅
        List<String> companyAnswers = List.of(request.getQ1(), request.getQ2(), request.getQ3());
        Map<String, Long> companyCount = companyAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans, Collectors.counting()));

        List<String> companyTypes = Arrays.stream(CompanyType.values())
                .map(CompanyType::getDisplayName)
                .toList();

        Map<String, Double> companyRatio = new LinkedHashMap<>();
        int baseScore = 1;
        double totalScore = 0.0;

        Map<String, Integer> scoreMap = new HashMap<>();
        for (String type : companyTypes) {
            int score = baseScore + companyCount.getOrDefault(type, 0L).intValue();
            scoreMap.put(type, score);
            totalScore += score;
        }

        for (String type : companyTypes) {
            companyRatio.put(type, scoreMap.get(type) / totalScore);
        }

        String companyTypeFinal = resolveFinal(companyCount, "all");

        // 2. 복지 카운팅
        List<String> welfareAnswers = List.of(request.getQ4(), request.getQ5(), request.getQ6());
        Map<String, Long> welfareCount = welfareAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans, Collectors.counting()));
        String welfareFinal = resolveFinal(welfareCount, "all");


        List<String> welfareList = welfareAnswers.stream()
                .distinct()
                .toList();

        // 3. DB에서 캐릭터 조회
        OnboardingCharacter character = onboardingRepository.findByCompanyTypeAndWelfare(companyTypeFinal, welfareFinal)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHARACTER_NOT_FOUND));

        // 4. Response DTO 반환
        return OnboardingDto.OnboardingResponse.builder()
                .companyTypeFinal(companyTypeFinal)
                .companyRatio(companyRatio)
                .welfareList(welfareList)
                .characterId(character.getId())
                .characterName(character.getCharacterName())
                .build();
    }

    private String resolveFinal(Map<String, Long> countMap, String fallback) {
        if (countMap.isEmpty()) return fallback;

        long max = countMap.values().stream()
                .mapToLong(v -> v)
                .max()
                .orElse(0);

        // max 득표인 항목 추출
        List<String> top = countMap.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .toList();

        // 동점이 아니면 1등 리턴, 동점이면 fallback
        return top.size() == 1 ? top.get(0) : fallback;
    }

//    public OnboardingDto.OnboardingSignupResponse onboardingSignup(OnboardingDto.OnboardingSignupRequest request) {
//        // user 확인
//        User user = userRepository.findById(jwtProvider.getCurrentUserId())
//                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));
//        // 데이터 저장
//        user.updateUsersInfo(request.getJobGroup(), );
//        // redis 저장
//    }
}
