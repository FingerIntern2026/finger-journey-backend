package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

// 미디어 업로드 응답 (API-ADM-017)
@Getter
@Builder
public class MediaUploadResponseDto {
    private Long mediaId;
    private String fileUrl;
}
