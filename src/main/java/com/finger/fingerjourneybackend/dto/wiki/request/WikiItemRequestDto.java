package com.finger.fingerjourneybackend.dto.wiki.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

// 위키 항목 상세 요청 (API-WIK-003)
@Getter
public class WikiItemRequestDto {

    @NotNull(message = "itemId는 필수입니다.")
    private Long itemId;
}
