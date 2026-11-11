// AdminWikiController
// 역할: 위키 카테고리·항목 관리 API
// - POST /admin/wiki/category/create|update|delete  (API-ADM-011 ~ 013)
// - POST /admin/wiki/item/create|update|delete      (API-ADM-014 ~ 016)
// 삭제 API는 명세서대로 응답 body 없이 HTTP 200만 반환한다.

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiCategoryCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiCategoryIdRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiCategoryUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiItemCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiItemIdRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiItemUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiCategoryCreateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiCategoryUpdateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiItemCreateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiItemUpdateResponseDto;
import com.finger.fingerjourneybackend.service.AdminWikiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/wiki")
@RequiredArgsConstructor
public class AdminWikiController {

    private final AdminWikiService adminWikiService;

    @PostMapping("/category/create")
    public ApiResponseDto<WikiCategoryCreateResponseDto> createCategory(
            @Valid @RequestBody WikiCategoryCreateRequestDto request) {
        return new ApiResponseDto<>(adminWikiService.createCategory(request));
    }

    @PostMapping("/category/update")
    public ApiResponseDto<WikiCategoryUpdateResponseDto> updateCategory(
            @Valid @RequestBody WikiCategoryUpdateRequestDto request) {
        return new ApiResponseDto<>(adminWikiService.updateCategory(request));
    }

    @PostMapping("/category/delete")
    public void deleteCategory(@Valid @RequestBody WikiCategoryIdRequestDto request) {
        adminWikiService.deleteCategory(request.getCategoryId());
    }

    @PostMapping("/item/create")
    public ApiResponseDto<WikiItemCreateResponseDto> createItem(
            @Valid @RequestBody WikiItemCreateRequestDto request) {
        return new ApiResponseDto<>(adminWikiService.createItem(request));
    }

    @PostMapping("/item/update")
    public ApiResponseDto<WikiItemUpdateResponseDto> updateItem(
            @Valid @RequestBody WikiItemUpdateRequestDto request) {
        return new ApiResponseDto<>(adminWikiService.updateItem(request));
    }

    @PostMapping("/item/delete")
    public void deleteItem(@Valid @RequestBody WikiItemIdRequestDto request) {
        adminWikiService.deleteItem(request.getItemId());
    }
}
