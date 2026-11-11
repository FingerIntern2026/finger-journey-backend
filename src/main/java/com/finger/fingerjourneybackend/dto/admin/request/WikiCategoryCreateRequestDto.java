package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;

// 위키 카테고리 등록 요청 (API-ADM-011)
// displayOrder를 안 보내면 맨 뒤에 붙는다.
@Getter
public class WikiCategoryCreateRequestDto {

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(max = 100, message = "카테고리명은 100자 이하여야 합니다.")
    private String categoryName;

    @NotBlank(message = "아이콘은 필수입니다.")
    @Size(max = 10, message = "아이콘은 10자 이하여야 합니다.")
    private String icon;

    @PositiveOrZero(message = "노출 순서는 0 이상이어야 합니다.")
    private Integer displayOrder;
}
