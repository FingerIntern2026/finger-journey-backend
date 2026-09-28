package com.finger.fingerjourneybackend.exception;

import com.finger.fingerjourneybackend.dto.ErrorResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void usesDefaultMessageFromErrorCode() {
        CustomException exception = new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND);

        ResponseEntity<ErrorResponseDto> response = handler.handleCustomException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND.getMessage());
    }

    @Test
    void usesCustomMessageWhileKeepingStatusAndErrorCode() {
        String customMessage = "사번 20260015에 해당하는 사원이 없습니다.";
        CustomException exception = new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND, customMessage);

        ResponseEntity<ErrorResponseDto> response = handler.handleCustomException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(customMessage);
    }
}
