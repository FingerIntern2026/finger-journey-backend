package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

// 진행현황 조회 응답 (API-ADM-008)
// stageProgress는 완료한 스테이지 수 / 전체 스테이지 수 (예: 3/9)
@Getter
@Builder
public class ProgressListResponseDto {
    private Long employeeId;
    private String name;
    private String currentPhase;
    private String stageProgress;
}
