package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 가방싸기 체크리스트 조회 응답 (API-ADM-009)
// status는 입사자용 API와 같은 NOT_STARTED / IN_PROGRESS / COMPLETED
@Getter
@Builder
public class BagChecklistResponseDto {
    private Long checkTypeId;
    private String checkName;
    private String status;
    private LocalDateTime checkedAt;
}
