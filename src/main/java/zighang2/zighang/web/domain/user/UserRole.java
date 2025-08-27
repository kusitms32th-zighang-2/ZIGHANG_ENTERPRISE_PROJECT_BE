package zighang2.zighang.web.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;

import java.util.Arrays;

public enum UserRole {
    GENERAL("일반 회원"),
    ADMIN("관리자"),
    EMPLOYED("기업 회원");


    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
