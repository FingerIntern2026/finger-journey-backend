package com.finger.fingerjourneybackend.dto.bag.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BagEnterRequestDto {

    @NotNull(message = "체크 항목 ID는 필수입니다.")
    private Long checkTypeId;
}