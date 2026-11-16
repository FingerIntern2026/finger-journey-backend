package com.finger.fingerjourneybackend.dto.wiki.response;

import lombok.Builder;
import lombok.Getter;

// 위키 검색 결과 한 건 (API-WIK-002)
// snippet은 본문 중 검색어 주변의 일부 글자다.
@Getter
@Builder
public class WikiSearchResponseDto {
    private Long itemId;
    private String title;
    private String categoryName;
    private String snippet;
}
