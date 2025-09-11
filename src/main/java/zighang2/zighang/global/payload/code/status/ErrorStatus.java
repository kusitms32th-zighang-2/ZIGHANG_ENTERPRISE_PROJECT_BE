package zighang2.zighang.global.payload.code.status;

import zighang2.zighang.global.payload.code.BaseErrorCode;
import zighang2.zighang.global.payload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 회원 관련 응답 1000
    USER_ID_NULL(HttpStatus.BAD_REQUEST, "USER_1001", "사용자 아이디는 필수 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_1002", "해당하는 사용자가 존재하지 않습니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "USER_1003", "닉네임은 필수 입니다."),
    EMAIL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "USER_1004", "이미 존재하는 이메일 입니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND,"ADDRESS_1005","사용자의 주소를 찾을 수 없습니다."),
    TRANSPORT_NOT_FOUND(HttpStatus.NOT_FOUND,"TRANSPORT_1006","사용자의 교통수단을 찾을 수 없습니다."),
    INVALID_TRANSPORT(HttpStatus.BAD_REQUEST,"USER_1007","지원하지않는 교통수단입니다."),
    MAXCOMMUTE_NOT_FOUND(HttpStatus.NOT_FOUND,"MAXCOMMUTE_1008","사용자의 최대 통근 시간을 찾을 수 없습니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "USER1101", "유효하지 않은 입력값입니다."),


    //인증 관련 에러 4000
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN4001", "잘못된 형식의 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4010", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4011", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN4002", "지원하지 않는 토큰입니다."),
    EMPTY_CLAIMS(HttpStatus.BAD_REQUEST, "TOKEN4003", "클레임이 비어있습니다."),
    EMPTY_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN4004", "비어있는 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TOKEN4040", "토큰을 찾을 수 없습니다."),
    BLOCKED_TOKEN(HttpStatus.FORBIDDEN, "TOKEN4019", "블랙리스트에 등록된 토큰입니다"),

    // 온보딩 관련 에러 5000
    CHARACTER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ONBOARDING_5001", "해당하는 캐릭터가 존재하지 않습니다."),

    // TMAP API 관련 에러 2000
    TMAP_COORDINATE_NOT_FOUND(HttpStatus.NOT_FOUND,"TMAP_2001","좌표를 찾을 수 없습니다."),
    TMAP_GEOCODING_EMPTY(HttpStatus.NOT_FOUND, "TMAP_2002", "지오코딩 결과가 비어 있습니다."),
    TMAP_GEOCODING_MAPPING_FAILED(HttpStatus.BAD_REQUEST, "TMAP_2003", "지오코딩 응답 매핑에 실패했습니다."),
    TMAP_DRIVING_EMPTY(HttpStatus.NOT_FOUND, "TMAP_2101", "자동차 경로 응답이 비어 있습니다."),
    TMAP_DRIVING_MAPPING_FAILED(HttpStatus.BAD_REQUEST, "TMAP_2102", "자동차 경로 응답에서 totalTime을 찾지 못했습니다."),
    TMAP_TRANSIT_EMPTY(HttpStatus.NOT_FOUND, "TMAP_2201", "대중교통 요약 응답에 itineraries가 없습니다."),
    TMAP_TRANSIT_MAPPING_FAILED(HttpStatus.BAD_REQUEST, "TMAP_2202", "대중교통 요약 응답에서 totalTime을 찾지 못했습니다."),

    // JOB 관련 에러 6000
    JOBRECOMMEND_NOT_FOUND(HttpStatus.NOT_FOUND, "JOBRECOMMEND_6001", "해당하는 공고가 존재하지 않습니다."),
    JOBGROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "JOBGROUP_6002", "해당하는 직군이 존재하지 않습니다."),
    JOBPOSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "JOBPOSITION_6003", "해당하는 직무가 존재하지 않습니다."),

    // COMPANY TYPE 관련 에러 7000
    COMPANY_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANYTYPE_7001","해당하는 기업 유형이 존재하지 않습니다."),

    // RecruitmentType 관련 에러 8000
    RECRUITMENT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT_TYPE_8001","해당하는 모집 유형이 존재하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}