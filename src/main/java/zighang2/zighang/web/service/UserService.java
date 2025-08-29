package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.UserDto;
import zighang2.zighang.web.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public UserDto.userModifyDto modifyUserInfo(UserDto.userModifyDto userModifyDto) {
        Long userId = jwtProvider.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        user.updateUsersInfo(userModifyDto.getUserRole(), userModifyDto.getJobGroup(), userModifyDto.getCompanySize(), userModifyDto.getEduation(), userModifyDto.getReceivingEmail(), userModifyDto.getWorkExperience());
        userRepository.save(user);

        User updatedUser = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        return userModifyDto.of(updatedUser);
    }
}
