// AdminMediaController
// 역할: 위키 이미지 등 관리자 파일 업로드 API
// - POST /admin/media/upload  (API-ADM-017, multipart/form-data)
//   요청 항목: file(파일), mediaType(IMAGE / ICON / BACKGROUND)
//   file과 mediaType 누락도 서비스에서 "지원하지 않는 파일" 오류(ADM_004)로 통일해 응답한다.

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.MediaUploadResponseDto;
import com.finger.fingerjourneybackend.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<MediaUploadResponseDto> upload(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "mediaType", required = false) String mediaType) {
        return new ApiResponseDto<>(mediaService.upload(file, mediaType));
    }
}
