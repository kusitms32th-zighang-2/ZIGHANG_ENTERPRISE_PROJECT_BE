package zighang2.zighang.web.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CompanyType {
    MAJOR("대기업"),
    MID_SIZE("중견기업"),
    FOREIGN("외국계"),
    UNICORN("유니콘"),
    STARTUP("스타트업"),
    SMALL_MEDIUM("중소기업"),
    MIXED("all");
    private final String displayName;


    CompanyType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
