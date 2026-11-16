package com.finger.fingerjourneybackend.dto.path.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PathStageClearResponseDto { //스테이지 완료 응답 DTO

    private String status;
    private LocalDateTime completedAt;
}