package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.UserDto;
import zighang2.zighang.web.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserDto.MypageModifyDto modifyUserInfo(UserDto.MypageModifyDto mypageModifyDto) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        user.updateUsersInfo(
                mypageModifyDto.getJobGroup(),
                mypageModifyDto.getCompanySize(),
                mypageModifyDto.getEducation(),
                mypageModifyDto.getWorkExperience(),
                mypageModifyDto.getReceivingEmail());
        userRepository.save(user);

        return mypageModifyDto.of(user);
    }

    public UserDto.MyPageDto getMypage() {
        Long userId = jwtProvider.getCurrentUserId();
        User user = userRepository.findById(userId).
                orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        return UserDto.MyPageDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .myPageModifyDto(UserDto.MypageModifyDto.of(user))
                .build();

    }
}
