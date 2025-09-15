package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.global.service.RedisService;
import zighang2.zighang.web.domain.CompanyType;
import zighang2.zighang.web.domain.JobGroup;
import zighang2.zighang.web.domain.JobPosition;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;
import zighang2.zighang.web.domain.enums.JobPositionEnum;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserCompanyType;
import zighang2.zighang.web.domain.user.UserJobPosition;
import zighang2.zighang.web.dto.SearchDto;
import zighang2.zighang.web.dto.UserDto;
import zighang2.zighang.web.repository.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@EnableAsync
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JobGroupRepository jobGroupRepository;
    private final JobPositionRepository jobPositionRepository;
    private final UserJobPositionRepository userJobPositionRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final UserCompanyTypeRepository userCompanyTypeRepository;
    private final RecommendService recommendService;
    private final JobRecommendRepository jobRecommendRepository;
    private final RedisService redisService;

    @Transactional
    public UserDto.MypageModifyResponse modifyUserInfo(UserDto.MypageModifyRequest mypageModifyRequest) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        JobGroup jobGroup = jobGroupRepository.findByJobGroupName(mypageModifyRequest.getJobGroups())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.JOBGROUP_NOT_FOUND));

        user.updateUsersJobGroup(jobGroup);

        List<UserJobPosition> existing = userJobPositionRepository.findByUser_Id(user.getId());
        Set<JobPositionEnum> newPositions = new HashSet<>(mypageModifyRequest.getJobPositions());

        for (UserJobPosition ujp : existing) {
            if (!newPositions.contains(ujp.getJobPosition().getJobPositionName())) {
                userJobPositionRepository.delete(ujp);
                userJobPositionRepository.flush();
            }
        }

        for (JobPositionEnum jobPositionEnum : newPositions) {
            boolean alreadyExists = existing.stream()
                    .anyMatch(ujp -> ujp.getJobPosition().getJobPositionName().equals(jobPositionEnum));

            if (!alreadyExists) {
                JobPosition jobPosition = jobPositionRepository
                        .findByJobPositionNameAndJobGroup(jobPositionEnum, jobGroup)
                        .orElseThrow(() -> new NotFoundHandler(ErrorStatus.JOBPOSITION_NOT_FOUND));

                UserJobPosition newUjp = UserJobPosition.builder()
                        .user(user)
                        .jobPosition(jobPosition)
                        .build();

                userJobPositionRepository.save(newUjp);
            }
        }

        List<UserCompanyType> companyTypeExisting = userCompanyTypeRepository.findByUserId(user.getId());
        Set<CompanyTypeEnum> newCompanyTypes = new HashSet<>(mypageModifyRequest.getCompanyTypes());

        for (UserCompanyType uct : companyTypeExisting) {
            if (!newCompanyTypes.contains(uct.getCompanyType().getCompanyTypeName())) {
                userCompanyTypeRepository.delete(uct);
                userCompanyTypeRepository.flush();
            }
        }

        for (CompanyTypeEnum companyTypeEnum : newCompanyTypes) {
            boolean alreadyExists = companyTypeExisting.stream()
                    .anyMatch(uct -> uct.getCompanyType().getCompanyTypeName().equals(companyTypeEnum));

            if (!alreadyExists) {
                CompanyType companyType = companyTypeRepository.findByCompanyTypeName(companyTypeEnum)
                        .orElseThrow(()->new NotFoundHandler(ErrorStatus.COMPANY_TYPE_NOT_FOUND));

                UserCompanyType uct = UserCompanyType.builder()
                        .user(user)
                        .companyType(companyType)
                        .build();
                userCompanyTypeRepository.save(uct);
            }
        }

        user.updateUsersInfo(
                mypageModifyRequest.getEducation(),
                mypageModifyRequest.getWorkExperience(),
                mypageModifyRequest.getAddress(),
                mypageModifyRequest.getTransport(),
                mypageModifyRequest.getMaxCommuteMinutes(),
                mypageModifyRequest.getReceivingEmail()
        );

        // jobRecommend 관련 데이터 모두 삭제
        jobRecommendRepository.deleteAllByUser(user);

        // Redis에서 기업비율 정보, 복지 리스트 가져오기
        List<String> welfareList = redisService.getWelfareList(userId);
        System.out.println("welfareList = " + welfareList.toString());
        Map<CompanyTypeEnum, Double> companyRatio = redisService.getCompanyRatio(userId);
        System.out.println("companyRatio = " + companyRatio.toString());

        // 비동기 처리 (공고 재추천 후 -> 저장)
        recommendService.getFullRecommendationsAsync(user, welfareList, companyRatio);

        return UserDto.MypageModifyResponse.of(user);
    }

    @Transactional
    public UserDto.MyPageDto getMypage() {
        Long userId = jwtProvider.getCurrentUserId();
        User user = userRepository.findById(userId).
                orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        List<String> jobPositions = user.getUserJobPositions().stream()
                .map(ujp -> ujp.getJobPosition().getJobPositionName().getDisplay())
                .toList();

        List<String> companyTypes = user.getUserCompanyTypes().stream()
                .map(uct-> uct.getCompanyType().getCompanyTypeName().getDisplay())
                .toList();


        UserDto.MypageModifyResponse modifyResponse = UserDto.MypageModifyResponse.builder()
                .jobGroups(user.getJobGroup().getJobGroupName().getDisplay())
                .jobPositions(jobPositions)
                .companyTypes(companyTypes)
                .education(user.getEducation().getDisplayName())
                .workExperience(user.getWorkExperience())
                .address(user.getAddress())
                .transport(String.valueOf(user.getTransport()))
                .maxCommuteMinutes(user.getMaxCommuteMinutes())
                .receivingEmail(user.getReceivingEmail())
                .build();

        return UserDto.MyPageDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .characterId(user.getOnboardingCharacter().getId())
                .characterName(user.getOnboardingCharacter().getCharacterName().getDisplayName())
                .MypageModifyResponse(modifyResponse)
                .build();

    }

    public UserDto.MyPageAllResponse getMypageAll() {
        Long userId = jwtProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        // 온보딩 시점 추천 6개
        List<JobRecommend> top6 = jobRecommendRepository.findTop6ByUserIdOrderByIdAsc(userId);

        List<SearchDto.SearchResponse_2> searchResponses = top6.stream()
                .map(SearchDto.SearchResponse_2::of)
                .toList();

        return UserDto.MyPageAllResponse.builder()
                .id(user.getId())
                .characterId(user.getOnboardingCharacter().getId())
                .characterName(user.getOnboardingCharacter().getCharacterName().getDisplayName())
                .searchResponses(searchResponses)
                .build();
    }

//    public UserDto reOnboarding() {
//        Long userId = jwtProvider.getCurrentUserId();
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));
//
//        userJobPositionRepository.deleteAll(user.getUserJobPositions());
//        userCompanyTypeRepository.deleteAll(user.getUserCompanyTypes());
//        jobRecommendRepository.deleteAll(user.getJobRecommendList());
//
//        // 2) 캐릭터 참조 해제
////        user.setOnboardingCharacter(null);
//    }
}
