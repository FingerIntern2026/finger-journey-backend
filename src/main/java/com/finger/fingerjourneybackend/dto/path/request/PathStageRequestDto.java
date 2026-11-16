package com.finger.fingerjourneybackend.dto.path.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PathStageRequestDto { //공통 스테이지 요청 DTO

    @NotNull(message = "스테이지 ID는 필수입니다.")
    private Long stageId;
}