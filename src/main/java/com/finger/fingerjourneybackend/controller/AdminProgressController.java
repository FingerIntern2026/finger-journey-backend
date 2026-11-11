// AdminProgressController
// 역할: 입사자별 온보딩 진행현황 API
// - POST /admin/progress/list  (API-ADM-008)
//   요청 body: { "organizationId": 3 } (선택, body 없이 보내도 전체 조회)

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.admin.request.AdminProgressListRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.ProgressListResponseDto;
import com.finger.fingerjourneybackend.service.AdminProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/progress")
@RequiredArgsConstructor
public class AdminProgressController {

    private final AdminProgressService adminProgressService;

    @PostMapping("/list")
    public ApiResponseDto<List<ProgressListResponseDto>> getProgressList(
            @RequestBody(required = false) AdminProgressListRequestDto request) {
        Long organizationId = request == null ? null : request.getOrganizationId();

        return new ApiResponseDto<>(adminProgressService.getProgressList(organizationId));
    }
}
