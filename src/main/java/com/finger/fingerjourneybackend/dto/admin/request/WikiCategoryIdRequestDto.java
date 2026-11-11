package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

// 위키 카테고리 삭제 요청 (API-ADM-013)
@Getter
public class WikiCategoryIdRequestDto {

    @NotNull(message = "categoryId는 필수입니다.")
    private Long categoryId;
}
