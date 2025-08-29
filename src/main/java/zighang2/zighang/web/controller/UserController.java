package zighang2.zighang.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import zighang2.zighang.global.payload.ApiResponse;
import zighang2.zighang.web.dto.UserDto;
import zighang2.zighang.web.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PatchMapping("")
    ApiResponse<?> modifyUsersInfo(@RequestBody UserDto.UserModifyDto userModifyDto) {
        return ApiResponse.onSuccess(userService.modifyUserInfo(userModifyDto));
    }
}
