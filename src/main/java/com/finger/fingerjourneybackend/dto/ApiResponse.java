package com.finger.fingerjourneybackend.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success = true;
    private final T data;

    public ApiResponse(T data) {
        this.data = data;
    }
}
