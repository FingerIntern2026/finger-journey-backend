package com.finger.fingerjourneybackend.dto.path.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PathStageEnterResponseDto { //스테이지 진입 응답 DTO

    private Boolean allowed;
    private String status;
}