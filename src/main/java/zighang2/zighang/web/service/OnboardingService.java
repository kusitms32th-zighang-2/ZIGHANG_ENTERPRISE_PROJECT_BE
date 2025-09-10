package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.JobGroup;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;
import zighang2.zighang.web.domain.OnboardingCharacter;
import zighang2.zighang.web.domain.enums.UserRole;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.OnboardingDto;
import zighang2.zighang.web.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JobGroupRepository jobGroupRepository;
    private final JobPositionRepository jobPositionRepository;
    private final UserJobPositionRepository userJobPositionRepository;

    public OnboardingDto.OnboardingResponse getOnboardingCharacter(OnboardingDto.OnboardingRequest request) {

        // 1. 기업규모 카운팅 & 비율 계산 로직
        List<CompanyTypeEnum> companyAnswers = List.of(
                (request.getQ1()),
                (request.getQ2()),
                (request.getQ3())
        );

        Map<CompanyTypeEnum, Long> companyCount = companyAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans, Collectors.counting()));

        List<CompanyTypeEnum> companyTypeEnums = Arrays.stream(CompanyTypeEnum.values())
                .filter(ct -> ct != CompanyTypeEnum.MIXED)
                .toList();

        Map<String, Double> companyRatio = new LinkedHashMap<>();
        int baseScore = 1;
        double totalScore = 0.0;

        Map<CompanyTypeEnum, Integer> scoreMap = new HashMap<>();
        for (CompanyTypeEnum type : companyTypeEnums) {
            int score = baseScore + companyCount.getOrDefault(type, 0L).intValue();
            scoreMap.put(type, score);
            totalScore += score;
        }

        for (CompanyTypeEnum type : companyTypeEnums) {
            companyRatio.put(type.getDisplay(), scoreMap.get(type) / totalScore);
        }

        CompanyTypeEnum companyTypeEnumFinal = resolveFinal(companyCount, CompanyTypeEnum.MIXED);

        // 2. 복지 카운팅
        List<String> welfareAnswers = List.of(request.getQ4(), request.getQ5(), request.getQ6());
        Map<String, Long> welfareCount = welfareAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans, Collectors.counting()));
        String welfareFinal = resolveFinal(welfareCount, "all");

        // 3. DB에서 캐릭터 조회
        OnboardingCharacter character = onboardingRepository.findByCompanyTypeAndWelfare(companyTypeEnumFinal.getDisplay(), welfareFinal)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHARACTER_NOT_FOUND));


        // 4. 사용자가 선택한 기업 규모, 복지
        List<String> welfareList = welfareAnswers.stream()
                .distinct()
                .toList();

        List<String> companyTypeEnumList = companyAnswers.stream()
                .distinct()
                .map(CompanyTypeEnum::getDisplay) // enum → displayName 문자열 변환
                .toList();

        // 5. Response DTO 반환
        return OnboardingDto.OnboardingResponse.builder()
                .companyTypeEnumList(companyTypeEnumList)
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

    private CompanyTypeEnum parseCompanyType(String input) {
        return Arrays.stream(CompanyTypeEnum.values())
                .filter(ct -> ct.getDisplay().equals(input)) // 한글 displayName 매칭
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid company type: " + input));
    }

    public OnboardingDto.OnboardingSignupResponse onboardingSignup(OnboardingDto.OnboardingSignupRequest request) {
        // 1. Character 조회
        OnboardingCharacter character = onboardingRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 캐릭터"));

        // 2. JobGroup 조회
        JobGroup jobGroup = jobGroupRepository.findByjobGroupName(request.getJobGroupEnum())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직군"));

        // 3. User 엔티티 생성
        User user = User.builder()
                .education(request.getEducation())
                .workExperience(Integer.valueOf(request.getWorkExperience()))
                .address(request.getAddress())
                .transport(request.getTransport())
                .maxCommuteMinutes(request.getMaxCommuteMinutes())
                .onboardingCharacter(character)
                .jobGroup(jobGroup)
                .userRole(UserRole.GENERAL)
                .build();





        // redis에 데이터 저장
        return null;
    }
}
