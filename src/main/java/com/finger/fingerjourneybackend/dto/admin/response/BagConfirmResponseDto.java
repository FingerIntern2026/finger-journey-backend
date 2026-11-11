package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 가방싸기 확인 처리 응답 (API-ADM-010)
@Getter
@Builder
public class BagConfirmResponseDto {
    private String status;
    private LocalDateTime checkedAt;
}
