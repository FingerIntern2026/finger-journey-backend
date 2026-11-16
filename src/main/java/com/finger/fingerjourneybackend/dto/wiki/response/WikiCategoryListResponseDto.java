package com.finger.fingerjourneybackend.dto.wiki.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 위키 목록 조회 응답: 카테고리와 그 아래 항목들 (API-WIK-001)
@Getter
@Builder
public class WikiCategoryListResponseDto {
    private Long categoryId;
    private String categoryName;
    private String icon;
    private List<WikiItemSummaryResponseDto> items;
}
