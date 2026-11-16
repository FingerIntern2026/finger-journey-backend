package com.finger.fingerjourneybackend.dto.path.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PathStageDetailResponseDto { //스테이지 상세 응답 DTO

    private String stageName;
    private String mediaUrl;
    private Integer displayOrder;
}