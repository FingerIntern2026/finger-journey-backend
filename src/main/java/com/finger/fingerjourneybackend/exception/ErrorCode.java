// ErrorCode
// 역할: API 명세서 "응답코드" 시트에 정의된 에러코드를 enum으로 관리
// HTTP 상태코드 / code / message를 한 곳에서 관리해서, 명세서가 바뀌면 여기만 수정하면 됨
//
// 사용 예: throw new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND)

package com.finger.fingerjourneybackend.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "요청이 잘못되었습니다. HTTP메소드, URL, 입력하신 데이터를 확인해주세요."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401", "토큰이 만료되었습니다. 다시 로그인을 시도해주세요."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 정보를 찾을 수 없습니다. 확인 후 다시 시도해주세요."),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "COMMON_409", "요청이 현재 리소스 상태와 충돌합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부에 오류가 발생해 작업을 수행할 수 없습니다. 잠시 후에 시도해주세요."),

    // Auth
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "사번 또는 생년월일이 일치하지 않습니다."),
    INVALID_SESSION(HttpStatus.UNAUTHORIZED, "AUTH_002", "세션이 유효하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."),

    // Bag
    BAG_CHECKLIST_INCOMPLETE(HttpStatus.BAD_REQUEST, "BAG_001", "전체 항목이 완료되지 않아 다음 단계로 진행할 수 없습니다."),
    BAG_CHECK_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "BAG_002", "존재하지 않는 체크 항목입니다."),

    // Path
    PREVIOUS_STAGE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "PTH_001", "이전 스테이지를 먼저 완료해야 합니다."),
    STAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "PTH_002", "존재하지 않는 스테이지입니다."),
    STAGE_NOT_ACCESSIBLE(HttpStatus.BAD_REQUEST, "PTH_003", "이미 완료되었거나 진입하지 않은 스테이지입니다."),

    // Quiz
    QUIZ_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "QUZ_001", "존재하지 않는 퀴즈 세트입니다."),
    QUIZ_ALREADY_ANSWERED(HttpStatus.CONFLICT, "QUZ_002", "이미 응답한 문항입니다."),

    // Completion
    ACROSTIC_LENGTH_MISMATCH(HttpStatus.BAD_REQUEST, "CPL_001", "입력 형식이 이름 글자 수와 일치하지 않습니다."),
    ACROSTIC_NOT_FOUND(HttpStatus.NOT_FOUND, "CPL_002", "저장된 3행시가 없습니다."),
    REPORT_PREREQUISITE_NOT_MET(HttpStatus.BAD_REQUEST, "CPL_003", "퀴즈 또는 3행시가 완료되지 않아 리포트를 생성할 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "CPL_004", "생성된 리포트가 없습니다."),

    // Wiki
    WIKI_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "WIK_001", "존재하지 않는 위키 항목입니다."),
    WIKI_CATEGORY_DUPLICATE(HttpStatus.CONFLICT, "WIK_002", "이미 존재하는 카테고리명입니다."),
    WIKI_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "WIK_003", "존재하지 않는 카테고리입니다."),
    WIKI_CATEGORY_HAS_CHILDREN(HttpStatus.CONFLICT, "WIK_004", "하위 항목이 존재하여 삭제할 수 없습니다."),

    // Chat
    CHAT_QUESTION_EMPTY(HttpStatus.BAD_REQUEST, "CHT_001", "질문 내용이 비어있습니다."),

    // Admin / Employee
    ADMIN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ADM_001", "관리자 권한이 없습니다."),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "ADM_002", "존재하지 않는 사원입니다."),
    EMPLOYEE_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "ADM_003", "이미 등록된 사번입니다."),
    UNSUPPORTED_FILE(HttpStatus.BAD_REQUEST, "ADM_004", "지원하지 않는 파일 형식이거나 용량이 초과되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}