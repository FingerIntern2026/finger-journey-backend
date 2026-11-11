package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 위키 카테고리 수정 응답 (API-ADM-012)
@Getter
@Builder
public class WikiCategoryUpdateResponseDto {
    private Long categoryId;
    private LocalDateTime updatedAt;
}
