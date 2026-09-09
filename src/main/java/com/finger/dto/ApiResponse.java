// ApiResponse
// 역할: 모든 API 응답을 하나의 공통 형식으로 감싸는 래퍼(Wrapper) 클래스
// 성공 시:  { "success": true,  "data": ... }
// 실패 시:  { "success": false, "errorCode": "...", "message": "..." }
//
// Controller에서 Service가 리턴한 결과(또는 예외 정보)를 이 클래스로 감싸서
// 응답하면, 프론트엔드는 항상 같은 구조({success, data} 또는 {success, errorCode, message})로
// 응답을 파싱할 수 있음.
//
// 사용 예:
// - 성공: ApiResponse.success(employee)
// - 실패: ApiResponse.error("EMPLOYEE_NOT_FOUND", "해당 직원을 찾을 수 없습니다.")

package com.finger.fingerjourneybackend.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;

    // 외부에서 직접 new로 생성하지 못하게 막고, 아래 static 메서드로만 생성하게 함
    private ApiResponse(boolean success, T data, String errorCode, String message) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.message = message;
    }

    // 성공 응답 생성
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    // 실패 응답 생성
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message);
    }
}