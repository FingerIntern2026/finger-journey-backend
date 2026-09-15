// CustomException
// 역할: 비즈니스 로직에서 의도적으로 발생시키는 예외
// ErrorCode를 들고 있어서, GlobalExceptionHandler가 이걸 그대로 ErrorResponse로 변환함
//
// 사용 예: throw new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND)

package com.finger.fingerjourneybackend.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}