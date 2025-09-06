package zighang2.zighang.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    @Operation(summary = "마이페이지 수정 API", description = "마이페이지 수정 API입니다.")
    @PatchMapping("/my-page")
    public ApiResponse<UserDto.MypageModifyDto> modifyUsersInfo(@Valid @RequestBody UserDto.MypageModifyDto mypageDto) {
        return ApiResponse.onSuccess(userService.modifyUserInfo(mypageDto));
    }

    @Operation(summary = "마이페이지 조회 API", description = "마이페이지 조회 API입니다.")
    @GetMapping("/my-page")
    public ApiResponse<UserDto.MyPageDto> getUsersInfo() {
        return ApiResponse.onSuccess(userService.getMypage());
    }
}
