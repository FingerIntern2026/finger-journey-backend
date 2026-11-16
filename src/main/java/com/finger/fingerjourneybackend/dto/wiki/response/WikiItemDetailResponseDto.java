package com.finger.fingerjourneybackend.dto.wiki.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 위키 항목 상세 응답 (API-WIK-003)
// mediaUrl은 첨부 이미지 주소들이다. (최대 3개, 표시 순서대로, 없으면 빈 배열)
@Getter
@Builder
public class WikiItemDetailResponseDto {
    private String title;
    private String content;
    private List<String> mediaUrl;
}
