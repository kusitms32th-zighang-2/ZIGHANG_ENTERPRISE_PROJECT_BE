package zighang2.zighang.web.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RecruitmentType {
    FULL_TIME("정규직"),
    CONTRACT("계약직"),
    DAY_WORKER("일용직"),
    CONVERTIBLE_INTERN("전환형 인턴"),
    EXPERIENTIAL_INTERN("채용형 인턴"),
    FREELANCER("프리랜서"),
    ALTERNATIVE_MILITARY_SERVICE("산업기능요원");

    private final String displayName;

    RecruitmentType(String displayName) {
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
