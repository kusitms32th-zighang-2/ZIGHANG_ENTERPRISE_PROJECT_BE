package zighang2.zighang.web.domain.enums;

import lombok.AllArgsConstructor;

public enum Education {
    IRRELEVANT(0,"무관"),
    HIGH_SCHOOL(1,"고졸"),
    JUNIOR_COLLEGE(2,"초대졸"),
    BACHELOR(3,"학사"),
    MASTER(4,"석사"),
    DOCTOR(5,"박사");

    private final Integer level;
    private final String displayName;

    Education(Integer level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Integer getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
