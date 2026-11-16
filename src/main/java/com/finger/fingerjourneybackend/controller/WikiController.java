// WikiController
// 역할: 입사자가 보는 핑거위키 조회 API
// - POST /wiki/list    (API-WIK-001) 요청 body 없음
// - POST /wiki/search  (API-WIK-002) 요청 body: { "keyword": "복합기" }
// - POST /wiki/item    (API-WIK-003) 요청 body: { "itemId": 1 }

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.wiki.request.WikiItemRequestDto;
import com.finger.fingerjourneybackend.dto.wiki.request.WikiSearchRequestDto;
import com.finger.fingerjourneybackend.dto.wiki.response.WikiCategoryListResponseDto;
import com.finger.fingerjourneybackend.dto.wiki.response.WikiItemDetailResponseDto;
import com.finger.fingerjourneybackend.dto.wiki.response.WikiSearchResponseDto;
import com.finger.fingerjourneybackend.service.WikiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wiki")
@RequiredArgsConstructor
public class WikiController {

    private final WikiService wikiService;

    @PostMapping("/list")
    public ApiResponseDto<List<WikiCategoryListResponseDto>> getWikiList() {
        return new ApiResponseDto<>(wikiService.getWikiList());
    }

    @PostMapping("/search")
    public ApiResponseDto<List<WikiSearchResponseDto>> search(@Valid @RequestBody WikiSearchRequestDto request) {
        return new ApiResponseDto<>(wikiService.search(request.getKeyword()));
    }

    @PostMapping("/item")
    public ApiResponseDto<WikiItemDetailResponseDto> getItem(@Valid @RequestBody WikiItemRequestDto request) {
        return new ApiResponseDto<>(wikiService.getItem(request.getItemId()));
    }
}
