package com.finger.fingerjourneybackend.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionResponseDto {
    private boolean valid;
    private Long employeeId;
    private String currentPhase;
}
