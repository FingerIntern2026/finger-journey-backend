package com.finger.fingerjourneybackend.dto.bag.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BagStatusItemResponseDto {

    private Long checkTypeId;
    private String status;
    private LocalDateTime checkedAt;
}