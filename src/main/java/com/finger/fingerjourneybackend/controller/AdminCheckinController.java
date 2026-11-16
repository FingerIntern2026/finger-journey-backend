// AdminCheckinController
// 역할: HR 관리 화면의 체크인 비밀번호 설정 API
// - POST /admin/checkin/password/update  (API-ADM-019) 요청 body: { "password": "..." }
//   성공 시 응답 data 없음 ({ "success": true })

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.config.OpenApiConfig;
import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.checkin.request.CheckinPasswordRequestDto;
import com.finger.fingerjourneybackend.service.CheckinPasswordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/admin/checkin")
@RequiredArgsConstructor
public class AdminCheckinController {

    private final CheckinPasswordService checkinPasswordService;

    @PostMapping("/password/update")
    public ApiResponseDto<Void> updatePassword(@Valid @RequestBody CheckinPasswordRequestDto request) {
        checkinPasswordService.update(request.getPassword());
        return new ApiResponseDto<>();
    }
}
