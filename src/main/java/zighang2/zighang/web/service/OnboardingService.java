package zighang2.zighang.web.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.global.service.RedisService;
import zighang2.zighang.web.domain.*;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;
import zighang2.zighang.web.domain.enums.JobPositionEnum;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserCompanyType;
import zighang2.zighang.web.domain.user.UserJobPosition;
import zighang2.zighang.web.dto.OnboardingDto;
import zighang2.zighang.web.dto.SearchDto;
import zighang2.zighang.web.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final JwtProvider jwtProvider;
    private final OnboardingRepository onboardingRepository;
    private final UserRepository userRepository;
    private final JobGroupRepository jobGroupRepository;
    private final JobPositionRepository jobPositionRepository;
    private final UserJobPositionRepository userJobPositionRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final UserCompanyTypeRepository userCompanyTypeRepository;
    private final RecommendService recommendService;
    private final RedisService redisService;

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
                .filter(ct -> ct != CompanyTypeEnum.PUBLIC && ct != CompanyTypeEnum.MIXED)
                .toList();

        Map<CompanyTypeEnum, Double> companyRatio = new LinkedHashMap<>();
        int baseScore = 1;
        double totalScore = 0.0;

        Map<CompanyTypeEnum, Integer> scoreMap = new HashMap<>();
        for (CompanyTypeEnum type : companyTypeEnums) {
            int score = baseScore + companyCount.getOrDefault(type, 0L).intValue();
            scoreMap.put(type, score);
            totalScore += score;
        }

        for (CompanyTypeEnum type : companyTypeEnums) {
            companyRatio.put(type, scoreMap.get(type) / totalScore);
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

        List<CompanyTypeEnum> companyTypeEnumList = companyAnswers.stream()
                .distinct()
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

    @Transactional
    public OnboardingDto.OnboardingSignupResponse onboardingSignup(OnboardingDto.OnboardingSignupRequest request) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저 온보딩 정보 Redis 저장.
        redisService.saveCompanyRatio(userId, request.getCompanyRatio());
        redisService.saveWelfareList(userId, request.getWelfareList());

        OnboardingCharacter character = onboardingRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.CHARACTER_NOT_FOUND));

        JobGroup jobGroup = jobGroupRepository.findByJobGroupName(request.getJobGroupEnum())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.JOBGROUP_NOT_FOUND));

        user.updateOnboardingInfo(
                request.getEducation(),
                request.getWorkExperience(),
                request.getAddress(),
                request.getTransport(),
                request.getMaxCommuteMinutes(),
                character,
                jobGroup
        );

        userRepository.save(user);

        if(request.getJobPositionEnum() != null && !request.getJobPositionEnum().isEmpty()) {
            for(JobPositionEnum jobPositionEnum : request.getJobPositionEnum()) {
                JobPosition jobPosition = jobPositionRepository.findByJobPositionName(jobPositionEnum)
                        .orElseThrow(() -> new NotFoundHandler(ErrorStatus .JOBPOSITION_NOT_FOUND));

                UserJobPosition userJobPosition = UserJobPosition.builder()
                        .jobPosition(jobPosition)
                        .user(user)
                        .build();

                userJobPositionRepository.save(userJobPosition);
            }
        }


        if (request.getCompanyList() != null && !request.getCompanyList().isEmpty()) {
            for (CompanyTypeEnum companyTypeEnum : request.getCompanyList()) {

                CompanyType companyType = companyTypeRepository.findByCompanyTypeName(companyTypeEnum)
                        .orElseThrow(() -> new NotFoundHandler(ErrorStatus.COMPANY_TYPE_NOT_FOUND));

                UserCompanyType userCompanyType = UserCompanyType.builder()
                        .user(user)
                        .companyType(companyType)
                        .build();

                userCompanyTypeRepository.save(userCompanyType);
            }
        }

        // 빠른 추천 (동기 처리 메서드) -> 응답에 포함
        List<JobRecommend> quickRecommendations = recommendService.getQuickRecommendations(
                user, request.getWelfareList());

        System.out.println(quickRecommendations);

        // 전체 추천 (비동기 처리 메서드)
        recommendService.getFullRecommendationsAsync(user, request.getWelfareList(), request.getCompanyRatio());

        // ================= 거리 필터링 =========================
        List<Map.Entry<JobRecommend,Integer>> commuteFilteredEntries = recommendService.calculateJobCommuteTimes(quickRecommendations,user);

        // 필터링된 추천 목록을 응답 DTO로 변환
        List<SearchDto.SearchResponse> recommendationDtoList = commuteFilteredEntries.stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(6)
                .map(jobRec -> SearchDto.SearchResponse.of(jobRec.getKey(),jobRec.getValue()))
                .collect(Collectors.toList());

        // 거리 필터링 후 OnboardingSignupResponse DTO 반환
        return OnboardingDto.OnboardingSignupResponse.builder()
                .characterId(character.getId())
                .characterName(character.getCharacterName().getDisplayName())
                .jobRecommends(recommendationDtoList)
                .build();

    }
}
