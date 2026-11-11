package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

// 위키 항목 등록 응답 (API-ADM-014)
@Getter
@Builder
public class WikiItemCreateResponseDto {
    private Long itemId;
}
