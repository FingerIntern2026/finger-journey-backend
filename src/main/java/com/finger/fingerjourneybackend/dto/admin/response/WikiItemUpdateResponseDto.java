package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 위키 항목 수정 응답 (API-ADM-015)
@Getter
@Builder
public class WikiItemUpdateResponseDto {
    private Long itemId;
    private LocalDateTime updatedAt;
}
