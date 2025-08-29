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

    public UserDto.UserModifyDto modifyUserInfo(UserDto.UserModifyDto userModifyDto) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        user.updateUsersInfo(userModifyDto.getJobGroup(), userModifyDto.getCompanySize(), userModifyDto.getEducation(), userModifyDto.getReceivingEmail(), userModifyDto.getWorkExperience());
        userRepository.save(user);

        User updatedUser = userRepository.findById(userId).orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        return userModifyDto.of(updatedUser);
    }
}
