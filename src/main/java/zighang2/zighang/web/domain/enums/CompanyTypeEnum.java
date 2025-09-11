package zighang2.zighang.web.domain.enums;

public enum CompanyTypeEnum {
        MAJOR("대기업"),
        MID_SIZE("중견기업"),
        FOREIGN("외국계"),
        UNICORN("유니콘"),
        STARTUP("스타트업"),
        SMALL_MEDIUM("중소기업"),
        PUBLIC("공공기관"),
        MIXED("all");

    private final String display;


    CompanyTypeEnum(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    @Override
    public String toString() {
        return display;
    }
}
