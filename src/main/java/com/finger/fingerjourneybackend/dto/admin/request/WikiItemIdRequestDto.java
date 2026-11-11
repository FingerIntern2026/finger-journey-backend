package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

// 위키 항목 삭제 요청 (API-ADM-016)
@Getter
public class WikiItemIdRequestDto {

    @NotNull(message = "itemId는 필수입니다.")
    private Long itemId;
}
