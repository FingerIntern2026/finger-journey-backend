// AdminCodeController
// 역할: 사원 등록·수정 폼의 드롭다운을 채우는 조직·직급 목록 API
// - POST /admin/org/list       (API-ADM-006)
// - POST /admin/position/list  (API-ADM-007)

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.OrganizationListResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.PositionListResponseDto;
import com.finger.fingerjourneybackend.service.AdminCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCodeController {

    private final AdminCodeService adminCodeService;

    @PostMapping("/org/list")
    public ApiResponseDto<List<OrganizationListResponseDto>> getOrganizationList() {
        return new ApiResponseDto<>(adminCodeService.getOrganizationList());
    }

    @PostMapping("/position/list")
    public ApiResponseDto<List<PositionListResponseDto>> getPositionList() {
        return new ApiResponseDto<>(adminCodeService.getPositionList());
    }
}
