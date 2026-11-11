package com.finger.fingerjourneybackend.dto.bag.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BagCompleteResponseDto {

    private String currentPhase;
}