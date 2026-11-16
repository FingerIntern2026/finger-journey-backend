package com.finger.fingerjourneybackend.dto.wiki.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 위키 항목 상세 응답 (API-WIK-003)
// mediaUrl은 첨부 이미지 주소들이다. (최대 3개, 표시 순서대로, 없으면 빈 배열)
// mediaId는 mediaUrl과 같은 순서의 이미지 ID들이다. 관리자가 항목을 수정할 때
// 기존 이미지를 그대로 두려면 이 ID를 다시 보내야 해서 함께 내려준다.
@Getter
@Builder
public class WikiItemDetailResponseDto {
    private String title;
    private String content;
    private List<String> mediaUrl;
    private List<Long> mediaId;
}
