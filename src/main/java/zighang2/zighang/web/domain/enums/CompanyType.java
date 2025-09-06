package zighang2.zighang.web.domain.enums;

public enum CompanyType {
    MAJOR("대기업"),
    MID_SIZE("중견기업"),
    FOREIGN("외국계"),
    UNICORN("유니콘 스타트업"),
    STARTUP("스타트업"),
    SMALL_MEDIUM("중소기업");

    private final String displayName;

    CompanyType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
