// AdminBagController
// 역할: HR 관리 화면의 가방싸기 체크리스트 API
// - POST /admin/bag/checklist  (API-ADM-009) 요청 body: { "employeeId": 1 }
// - POST /admin/bag/confirm    (API-ADM-010) 요청 body: { "employeeId": 1, "checkTypeId": 1 }
//   확인한 관리자의 사원 ID는 토큰에서 꺼내 기록한다. (AdminAuthInterceptor가 요청에 담아 둠)

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.config.AdminAuthInterceptor;
import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.admin.request.BagConfirmRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeIdRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.BagChecklistResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.BagConfirmResponseDto;
import com.finger.fingerjourneybackend.service.AdminBagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/bag")
@RequiredArgsConstructor
public class AdminBagController {

    private final AdminBagService adminBagService;

    @PostMapping("/checklist")
    public ApiResponseDto<List<BagChecklistResponseDto>> getChecklist(
            @Valid @RequestBody EmployeeIdRequestDto request) {
        return new ApiResponseDto<>(adminBagService.getChecklist(request.getEmployeeId()));
    }

    @PostMapping("/confirm")
    public ApiResponseDto<BagConfirmResponseDto> confirm(
            @Valid @RequestBody BagConfirmRequestDto request,
            @RequestAttribute(name = AdminAuthInterceptor.ADMIN_EMPLOYEE_ID_ATTRIBUTE, required = false)
            Long adminEmployeeId) {
        return new ApiResponseDto<>(adminBagService.confirm(
                request.getEmployeeId(), request.getCheckTypeId(), adminEmployeeId));
    }
}
