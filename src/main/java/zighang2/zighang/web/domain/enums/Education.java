package zighang2.zighang.web.domain.enums;

public enum Education {
    IRRELEVANT("무관"),
    HIGH_SCHOOL("고졸"),
    JUNIOR_COLLEGE("초대졸"),
    BACHELOR("학사"),
    MASTER("석사"),
    DOCTOR("박사");

    private final String displayName;

    Education(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
