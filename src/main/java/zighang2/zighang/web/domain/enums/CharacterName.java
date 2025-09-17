package zighang2.zighang.web.domain.enums;


import com.fasterxml.jackson.annotation.JsonValue;

public enum CharacterName {
    // 휴가 관련
    WORABEL_BELIEVER("워라밸 신봉자"),
    VACATION_HUNTER("휴가 헌터"),
    REFRESH_EXPLORER("리프레시 탐험가"),
    WORABEL_WARRIOR("워라밸 용사"),
    OFFICE_ESCAPE("오피스 탈출러"),
    WORKATION_MASTER("워케이션 마스터"),
    VACANCE_MAGICIAN("바캉스 마법사"),

    // 커리어 관련
    CAREER_ROCKET("커리어 로켓"),
    SKILL_MASTER("스킬 마스터"),
    LEVELUP_MASTER("레벨업 장인"),
    ROCKET_GROWER("로켓 성장러"),
    SEMINAR_WARRIOR("세미나 전사"),
    CAREER_CHAMELEON("커리어 카멜레온"),
    SKILLUP_MANIA("스킬업 매니아"),

    // 식사 관련
    BABSIM_CHAMPION("밥심 챔피언"),
    COMPANY_MEAL_LOVER("사내밥 애호가"),
    LUNCH_WORLD_TOURER("점심 월드투어러"),
    MEAL_SUPPORTER("식사 서포터즈"),
    MENU_ADVENTURER("메뉴 모험가"),
    BABSIM_INNOVATOR("밥심 혁신러"),
    LUNCH_POWERER("점심 파워러"),

    // 출퇴근 관련
    OFFWORK_MASTER("칼퇴 마스터"),
    COMMUTE_NINJA("출퇴근 닌자"),
    COMMUTE_RACER("출퇴근 레이서"),
    TELEPORT_MASTER("순간이동 장인"),
    COMMUTE_GAMBLER("출근 겜블러"),
    SHUTTLE_JUMPER("셔틀 점핑러"),
    COMMUTE_TRANSCENDER("출퇴근 초월자"),

    // 생존/라이프 관련
    FULL_OPTION_DREAMER("풀옵션 드리머"),
    PRACTICAL_CHAMPION("실속 챔피언"),
    SURVIVAL_MASTER("생존 마스터"),
    ENDURANCE_WARRIOR("버티는 용사"),
    CHALLENGE_MANIA("도전광인러"),
    FREE_WORKER("프리워커"),
    GREEDY_PIONEER("탐욕의 개척자");

    private final String displayName;

    CharacterName(String displayName) {
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
