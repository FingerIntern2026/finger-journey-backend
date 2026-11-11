package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;

// 위키 카테고리 수정 요청 (API-ADM-012)
// categoryId만 필수, 나머지는 보낸 값만 수정한다.
@Getter
public class WikiCategoryUpdateRequestDto {

    @NotNull(message = "categoryId는 필수입니다.")
    private Long categoryId;

    @Size(max = 100, message = "카테고리명은 100자 이하여야 합니다.")
    @Pattern(regexp = ".*\\S.*", message = "카테고리명은 비워둘 수 없습니다.")
    private String categoryName;

    @Size(max = 10, message = "아이콘은 10자 이하여야 합니다.")
    @Pattern(regexp = ".*\\S.*", message = "아이콘은 비워둘 수 없습니다.")
    private String icon;

    @PositiveOrZero(message = "노출 순서는 0 이상이어야 합니다.")
    private Integer displayOrder;
}
