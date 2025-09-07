package zighang2.zighang.web.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum JobGroup {
    전체("전체"),
    IT_개발("IT/개발"),
    AI_데이터("AI/데이터"),
    게임("게임"),
    디자인("디자인"),
    기획_전략("기획/전략"),
    마케팅_광고_홍보("마케팅/광고/홍보"),
    상품기획_MD("상품기획/MD"),
    영업("영업"),
    무역_물류_유통("무역/물류/유통"),
    운송_배송("운송/배송"),
    법률_법무("법률/법무"),
    HR_총무("HR/총무"),
    회계_세무_재무("회계/세무/재무"),
    증권_운영("증권/운영"),
    은행_카드_보험("은행/카드/보험"),
    엔지니어링_RnD("엔지니어링/R&D"),
    건설_건축("건설/건축"),
    생산_기능직("생산/기능직"),
    의료_보건("의료/보건"),
    공공_복지("공공/복지"),
    교육("교육"),
    미디어_엔터("미디어/엔터"),
    고객상담_TM("고객상담/TM"),
    서비스("서비스"),
    식음료("식음료");

    private final String display;

    JobGroup(String display) {
        this.display = display;
    }

    @JsonValue
    public String getDisplay() {
        return display;
    }
}
