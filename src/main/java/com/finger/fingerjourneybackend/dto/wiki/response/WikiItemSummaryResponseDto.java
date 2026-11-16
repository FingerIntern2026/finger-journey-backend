package com.finger.fingerjourneybackend.dto.wiki.response;

import lombok.Builder;
import lombok.Getter;

// 위키 목록에서 카테고리 아래에 나오는 항목 한 줄 (API-WIK-001)
// 본문은 담지 않는다. 항목을 펼칠 때 상세 조회(API-WIK-003)로 가져온다.
@Getter
@Builder
public class WikiItemSummaryResponseDto {
    private Long itemId;
    private String title;
    private String icon;
}
