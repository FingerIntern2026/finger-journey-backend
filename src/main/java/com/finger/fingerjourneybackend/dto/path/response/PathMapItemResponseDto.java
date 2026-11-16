package com.finger.fingerjourneybackend.dto.path.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PathMapItemResponseDto { //오솔길 맵 항목 응답 DTO

    private Long stageId;
    private String stageName;
    private Integer stageOrder;
    private String status;
}