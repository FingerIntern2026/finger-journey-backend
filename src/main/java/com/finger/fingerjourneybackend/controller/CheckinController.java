// CheckinController
// 역할: 입사자가 체크인 화면에 들어갈 때 쓰는 비밀번호 확인 API
// - POST /checkin/password/verify  (API-CHK-001) 요청 body: { "password": "..." }
//   맞으면 { "success": true }, 틀리면 CHK_001 오류

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.checkin.request.CheckinPasswordRequestDto;
import com.finger.fingerjourneybackend.service.CheckinPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinPasswordService checkinPasswordService;

    @PostMapping("/password/verify")
    public ApiResponseDto<Void> verifyPassword(@Valid @RequestBody CheckinPasswordRequestDto request) {
        checkinPasswordService.verify(request.getPassword());
        return new ApiResponseDto<>();
    }
}
