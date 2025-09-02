package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public UserDto.mypageModifyDto modifyUserInfo(UserDto.mypageModifyDto mypageModifyDto) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        user.updateUsersInfo(mypageModifyDto.getJobGroup(), mypageModifyDto.getCompanySize(), mypageModifyDto.getEducation(), mypageModifyDto.getReceivingEmail(), mypageModifyDto.getWorkExperience());
        userRepository.save(user);

        return mypageModifyDto.of(user);
    }

    public UserDto.myPageDto getUserInfo() {
        Long userId = jwtProvider.getCurrentUserId();
        User user = userRepository.findById(userId).
                orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        return UserDto.myPageDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .myPageModifyDto(UserDto.mypageModifyDto.of(user))
                .build();

    }
}
