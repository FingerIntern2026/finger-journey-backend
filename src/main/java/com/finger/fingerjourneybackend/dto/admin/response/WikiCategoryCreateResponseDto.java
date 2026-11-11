package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

// 위키 카테고리 등록 응답 (API-ADM-011)
@Getter
@Builder
public class WikiCategoryCreateResponseDto {
    private Long categoryId;
}
