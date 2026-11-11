package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

// 위키 항목 등록 요청 (API-ADM-014)
// mediaId는 명세서 이름 그대로이며, 첨부 이미지 ID 배열(최대 3개)이다.
@Getter
public class WikiItemCreateRequestDto {

    @NotNull(message = "categoryId는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    @NotBlank(message = "아이콘은 필수입니다.")
    @Size(max = 10, message = "아이콘은 10자 이하여야 합니다.")
    private String icon;

    @Size(max = 3, message = "이미지는 최대 3개까지 첨부할 수 있습니다.")
    private List<Long> mediaId;
}
