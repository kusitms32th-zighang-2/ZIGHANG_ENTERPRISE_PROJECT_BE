package zighang2.zighang.web.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    GENERAL("일반 회원"),
    ADMIN("관리자"),
    EMPLOYED("기업 회원");

    private final String description;

}
