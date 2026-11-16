package com.finger.fingerjourneybackend.dto.wiki.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// 위키 검색 요청 (API-WIK-002)
@Getter
public class WikiSearchRequestDto {

    @NotBlank(message = "검색어는 필수입니다.")
    private String keyword;
}
