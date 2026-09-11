// ErrorResponse
// 역할: API 실패 시 반환하는 공통 에러 응답 형식
// API 명세서 기준: 실패 시 { "success": false, "code": "...", "message": "..." } 형태로 반환
//
// 사용 예: new ErrorResponse("COMMON_404", "해당 직원을 찾을 수 없습니다.")

package com.finger.fingerjourneybackend.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final boolean success = false;
    private final String code;
    private final String message;
}