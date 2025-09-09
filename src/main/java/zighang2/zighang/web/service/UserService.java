package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.CompanyType;
import zighang2.zighang.web.domain.JobGroup;
import zighang2.zighang.web.domain.JobPosition;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;
import zighang2.zighang.web.domain.enums.JobPositionEnum;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserJobPosition;
import zighang2.zighang.web.dto.UserDto;
import zighang2.zighang.web.repository.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JobGroupRepository jobGroupRepository;
    private final JobPositionRepository jobPositionRepository;
    private final UserJobPositionRepository userJobPositionRepository;
    private final CompanyTypeRepository companyTypeRepository;

    @Transactional
    public UserDto.MypageModifyResponse modifyUserInfo(UserDto.MypageModifyRequest mypageModifyRequest) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        JobGroup jobGroup = jobGroupRepository.findByjobGroupName(mypageModifyRequest.getJobGroups())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.JOBGROUP_NOT_FOUND));

        user.updateUsersJobGroup(jobGroup);

        List<UserJobPosition> existing = userJobPositionRepository.findByUser_Id(user.getId());
        Set<JobPositionEnum> newPositions = new HashSet<>(mypageModifyRequest.getJobPositions());

        for (UserJobPosition ujp : existing) {
            if (!newPositions.contains(ujp.getJobPosition().getJobPositionName())) {
                userJobPositionRepository.delete(ujp);
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

        Set<CompanyTypeEnum> newCompanyTypes = new HashSet<>(mypageModifyRequest.getCompanyTypes());

        for (CompanyTypeEnum companyTypeEnum : newCompanyTypes) {
            CompanyType companyType = CompanyType.builder()
                    .user(user)
                    .companyType(companyTypeEnum)
                    .build();

            companyTypeRepository.save(companyType);
        }

        user.updateUsersInfo(
                mypageModifyRequest.getEducation(),
                mypageModifyRequest.getWorkExperience(),
                mypageModifyRequest.getAddress(),
                mypageModifyRequest.getTransport(),
                mypageModifyRequest.getMaxCommuteMinutes(),
                mypageModifyRequest.getReceivingEmail()
        );

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

        List<String> companyTypes = user.getCompanyTypeList().stream()
                .map(companyType -> companyType.getCompanyType().getDisplay())
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
                .MypageModifyResponse(modifyResponse)
                .build();

    }
}
