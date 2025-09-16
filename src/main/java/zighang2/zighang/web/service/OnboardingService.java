package zighang2.zighang.web.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import zighang2.zighang.web.dto.CalculationResult;
import zighang2.zighang.web.dto.OnboardingDto;
import zighang2.zighang.web.dto.SearchDto;
import zighang2.zighang.web.dto.UserDto;
import zighang2.zighang.web.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    private final JobRecommendRepository jobRecommendRepository;

    @Transactional
    public UserDto.MypageResponseDto onboardingSignup(UserDto.MypageRequestDto request){
        User user = userRepository.findById(jwtProvider.getCurrentUserId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        JobGroup jobGroup = jobGroupRepository.findByJobGroupName(request.getJobGroups())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.JOBGROUP_NOT_FOUND));

        user.updateOnboardingInfo(
                request.getEducation(),
                request.getWorkExperience(),
                request.getAddress(),
                request.getTransport(),
                request.getMaxCommuteMinutes(),
                jobGroup
        );

        saveUserJobPositions(user,request.getJobPositions());

        return UserDto.MypageResponseDto.of(user);
    }

    @Transactional
    public OnboardingDto.OnboardingResponse getCharacter(OnboardingDto.OnboardingRequest request) {

        CalculationResult calculationResult=calculateCompanyAndWelfare(request);

        // 3. DB에서 캐릭터 조회
        OnboardingCharacter character = onboardingRepository.findByCompanyTypeAndWelfare(calculationResult.companyTypeDisplay, calculationResult.getWelfareFinal())
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHARACTER_NOT_FOUND));


        // 4. 사용자가 선택한 기업 규모, 복지
        List<String> welfareList = calculationResult.welfareAnswers.stream()
                .distinct()
                .toList();

        List<CompanyTypeEnum> companyTypeEnumList = calculationResult.companyAnswers.stream()
                .distinct()
                .toList();

        // 5. Response DTO 반환
        return OnboardingDto.OnboardingResponse.builder()
                .companyTypeEnumList(companyTypeEnumList)
                .companyRatio(calculationResult.companyRatio)
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
    public OnboardingDto.OnboardingSignupResponse onboardingSignupAfterTest(OnboardingDto.OnboardingSignupRequest request) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저 테스트 정보 Redis 저장.
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
                jobGroup
        );

        user.updateOnboardingCharacter(character);

        userRepository.save(user);

        saveUserJobPositions(user,request.getJobPositionEnum());
        saveUserCompanyTypes(user,request.getCompanyList());

        return processRecommendationsAndBuildResponse(user,request.getWelfareList());
    }

    @Transactional
    public OnboardingDto.OnboardingSignupResponse getCharacterAfterOnboarding(OnboardingDto.OnboardingRequest request){

        CalculationResult calculationResult = calculateCompanyAndWelfare(request);


        // 4. 사용자가 선택한 기업 규모, 복지
        List<String> welfareList = calculationResult.welfareAnswers.stream()
                .distinct()
                .toList();

        List<CompanyTypeEnum> companyTypeEnumList = calculationResult.companyAnswers.stream()
                .distinct()
                .toList();

        User user = userRepository.findById(jwtProvider.getCurrentUserId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        OnboardingCharacter character = onboardingRepository.findByCompanyTypeAndWelfare(calculationResult.companyTypeDisplay, calculationResult.welfareFinal)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHARACTER_NOT_FOUND));

        user.updateOnboardingCharacter(character);
        userRepository.save(user);

        redisService.saveCompanyRatio(user.getId(), calculationResult.companyRatio);
        redisService.saveWelfareList(user.getId(),welfareList);

        saveUserCompanyTypes(user,companyTypeEnumList);

        return processRecommendationsAndBuildResponse(user,welfareList);
    }


    private CalculationResult calculateCompanyAndWelfare(OnboardingDto.OnboardingRequest request) {
        List<CompanyTypeEnum> companyAnswers = List.of(request.getQ1(), request.getQ2(), request.getQ3());

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

        List<String> welfareAnswers = List.of(request.getQ4(), request.getQ5(), request.getQ6());
        Map<String, Long> welfareCount = welfareAnswers.stream()
                .collect(Collectors.groupingBy(ans -> ans, Collectors.counting()));
        String welfareFinal = resolveFinal(welfareCount, "all");

        String companyTypeDisplay = resolveFinal(companyCount, CompanyTypeEnum.MIXED).getDisplay();

        return CalculationResult.builder()
                .welfareAnswers(welfareAnswers)
                .companyAnswers(companyAnswers)
                .companyRatio(companyRatio)
                .welfareFinal(welfareFinal)
                .companyTypeDisplay(companyTypeDisplay)
                .build();

    }

    private void saveUserJobPositions(User user, List<JobPositionEnum> jobPositionEnums) {
        if (jobPositionEnums != null && !jobPositionEnums.isEmpty()) {
            for (JobPositionEnum jobPositionEnum : jobPositionEnums) {
                JobPosition jobPosition = jobPositionRepository.findByJobPositionName(jobPositionEnum)
                        .orElseThrow(() -> new NotFoundHandler(ErrorStatus.JOBPOSITION_NOT_FOUND));
                UserJobPosition userJobPosition = UserJobPosition.builder()
                        .jobPosition(jobPosition)
                        .user(user)
                        .build();
                userJobPositionRepository.save(userJobPosition);
            }
        }
    }

    private void saveUserCompanyTypes(User user, List<CompanyTypeEnum> companyTypeEnums) {
        if (companyTypeEnums != null && !companyTypeEnums.isEmpty()) {
            for (CompanyTypeEnum companyTypeEnum : companyTypeEnums) {
                CompanyType companyType = companyTypeRepository.findByCompanyTypeName(companyTypeEnum)
                        .orElseThrow(() -> new NotFoundHandler(ErrorStatus.COMPANY_TYPE_NOT_FOUND));
                UserCompanyType userCompanyType = UserCompanyType.builder()
                        .user(user)
                        .companyType(companyType)
                        .build();
                userCompanyTypeRepository.save(userCompanyType);
            }
        }
    }

    private OnboardingDto.OnboardingSignupResponse processRecommendationsAndBuildResponse(User user, List<String> welfareList) {
        List<JobRecommend> quickRecommendations = recommendService.getQuickRecommendations(user, welfareList);

        List<Map.Entry<JobRecommend,Integer>> commuteFiltered = recommendService.calculateJobCommuteTimes(quickRecommendations, user);

        List<Map.Entry<JobRecommend,Integer>> top6 = commuteFiltered.stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(6)
                .toList();

        List<JobRecommend> top6Jobs = top6.stream()
                .map(entry -> {
                    JobRecommend job = entry.getKey();
                    job.setCommuteMinutes(entry.getValue());
                    return job;
                })
                .toList();

        jobRecommendRepository.saveAll(top6Jobs);
        jobRecommendRepository.flush();

        List<SearchDto.SearchResponse> recommendationDtoList = top6.stream()
                .map(entry -> SearchDto.SearchResponse.of(entry.getKey(), entry.getValue()))
                .toList();

        return OnboardingDto.OnboardingSignupResponse.builder()
                .characterId(user.getOnboardingCharacter().getId())
                .characterName(user.getOnboardingCharacter().getCharacterName().getDisplayName())
                .jobRecommends(recommendationDtoList)
                .build();
    }
}
