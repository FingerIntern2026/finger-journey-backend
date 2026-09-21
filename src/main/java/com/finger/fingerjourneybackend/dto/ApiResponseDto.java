package com.finger.fingerjourneybackend.dto;

import lombok.Getter;

@Getter
public class ApiResponseDto<T> {

    private final boolean success = true;
    private final T data;

    public ApiResponseDto(T data) {
        this.data = data;
    }
}
