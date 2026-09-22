// GlobalExceptionHandler
// 역할: 프로젝트 전체에서 발생하는 예외를 한 곳에서 잡아서, API 명세서 기준
// 에러 응답 형식
// ({ "success": false, "data": null, "errorCode": "...", "message": "..." })으로 변환해주는 공통 처리기
//
// - CustomException: ErrorCode에 정의된 HTTP 상태와 오류 내용을 응답에 반영
//   업무 조건 미충족은 HTTP 200 + success:false로, 실제 HTTP 오류는 4xx/5xx로 구분
// - MethodArgumentNotValidException: @Valid 검증 실패 (예: employeeId가 null)
// - 그 외 예상 못한 예외: COMMON_500으로 통일해서 응답 (스택트레이스 노출 방지)
//
// 예: Service에서 이렇게 던지면
//     throw new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND)
// 이 핸들러가 잡아서 아래처럼 응답함
//     HTTP 404,
//     { "success": false, "data": null, "errorCode": "ADM_002", "message": "존재하지 않는 사원입니다." }

package com.finger.fingerjourneybackend.exception;

import com.finger.fingerjourneybackend.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponseDto(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponseDto(ErrorCode.INVALID_REQUEST.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception e) {
        // 예상 못한 예외는 COMMON_500으로 감추더라도, 로그엔 실제 원인을 남겨야 디버깅 가능
        log.error("예상하지 못한 예외 발생", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponseDto(errorCode.getCode(), errorCode.getMessage()));
    }
}
