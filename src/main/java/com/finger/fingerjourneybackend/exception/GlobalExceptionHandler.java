// GlobalExceptionHandler
// 역할: 프로젝트 전체에서 발생하는 예외를 한 곳에서 잡아서, API 명세서 기준
// 에러 응답 형식({ "code": "...", "message": "..." })으로 변환해주는 공통 처리기
//
// 각 Controller/Service에서 개별적으로 try-catch를 쓰지 않아도,
// ResponseStatusException을 던지기만 하면 여기서 자동으로 잡아서 응답을 만들어줌
//
// 예: Service에서 이렇게 던지면
//     throw new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMON_404: 해당 직원을 찾을 수 없습니다.")
// 이 핸들러가 잡아서 아래처럼 응답함
//     HTTP 404, { "code": "COMMON_404", "message": "해당 직원을 찾을 수 없습니다." }

package com.finger.fingerjourneybackend.exception;

import com.finger.fingerjourneybackend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ResponseStatusException을 잡아서 ErrorResponse 형태로 변환
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());

        // reason 형식이 "COMMON_404: 해당 직원을 찾을 수 없습니다." 이런 식이라
        // ':' 기준으로 code와 message를 분리함
        String reason = e.getReason() != null ? e.getReason() : status.name();
        String code;
        String message;

        if (reason.contains(":")) {
            String[] parts = reason.split(":", 2);
            code = parts[0].trim();
            message = parts[1].trim();
        } else {
            code = status.name();
            message = reason;
        }

        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }
}