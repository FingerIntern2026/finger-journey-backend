package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

// 위키 항목 수정 요청 (API-ADM-015)
// itemId만 필수, 나머지는 보낸 값만 수정한다.
// mediaId를 보내면 첨부 이미지를 통째로 교체하고(빈 배열이면 모두 제거), 안 보내면 그대로 둔다.
@Getter
public class WikiItemUpdateRequestDto {

    @NotNull(message = "itemId는 필수입니다.")
    private Long itemId;

    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    @Pattern(regexp = ".*\\S.*", message = "제목은 비워둘 수 없습니다.")
    private String title;

    @Pattern(regexp = "(?s).*\\S.*", message = "본문은 비워둘 수 없습니다.")
    private String content;

    @Size(max = 10, message = "아이콘은 10자 이하여야 합니다.")
    @Pattern(regexp = ".*\\S.*", message = "아이콘은 비워둘 수 없습니다.")
    private String icon;

    @Size(max = 3, message = "이미지는 최대 3개까지 첨부할 수 있습니다.")
    private List<Long> mediaId;
}
