// EmployeeController
// 역할: HTTP 요청을 받아서 EmployeeService를 호출하고, 결과를 반환하는 계층
// 실제 비즈니스 로직(DB 조회/저장)은 여기 두지 않고 Service에 위임할 것
//
// 응답 형식 (API 명세서 기준):
// - 성공 시: ApiResponseDto로 감싸서 { "success": true, "data": {...} } 형태로 반환 (HTTP 200)
//   (단, 삭제처럼 명세서상 Body 없음이 명시된 API는 감싸지 않고 void 반환)
// - 실패 시: { "success": false, "code": "...", "message": "..." } 형태로 반환
//   (예외는 exception/GlobalExceptionHandler.java 에서 전역으로 처리함)

package com.finger.fingerjourneybackend.controller;


import com.finger.fingerjourneybackend.dto.admin.request.EmployeeCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeCreateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeUpdateResponseDto;
import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeIdRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeDetailResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeListResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // 목록조회: POST /admin/employee/list
    @PostMapping("/list")
    public ApiResponseDto<List<EmployeeListResponseDto>> getEmployeeList() {
        return new ApiResponseDto<>(employeeService.getEmployeeList());
    }

    // 상세조회: POST /admin/employee/detail
    // 요청 body: { "employeeId": 1 }
    // @Valid: EmployeeIdRequestDto의 @NotNull 검증을 여기서 실제로 수행함
    @PostMapping("/detail")
    public ApiResponseDto<EmployeeDetailResponseDto> getEmployeeDetail(@Valid @RequestBody EmployeeIdRequestDto request) {
        Employee employee = employeeService.getEmployeeDetail(request.getEmployeeId());
        return new ApiResponseDto<>(EmployeeDetailResponseDto.from(employee));
    }

    // 삭제: POST /admin/employee/delete
    // 요청 body: { "employeeId": 1 }
    // 명세서 기준 성공 시 응답 body 없음 (HTTP 200만 반환)
    @PostMapping("/delete")
    public void deleteEmployee(@Valid @RequestBody EmployeeIdRequestDto request) {
        employeeService.deleteEmployee(request.getEmployeeId());
    }


    /**
     * 사원 등록 
     * 요청 body: employeeNo, name, organizationId, positionId, hireDate
     * 성공 응답: { "success": true, "data": { "employeeId": 5 } }
     */
    @PostMapping("/create")
    public ApiResponseDto<EmployeeCreateResponseDto> createEmployee(@Valid @RequestBody EmployeeCreateRequestDto request) {
        Employee saved = employeeService.createEmployee(request);
        return new ApiResponseDto<>(EmployeeCreateResponseDto.from(saved));
 
    }

    /**
     * 사원 수정 (지연 담당, ADM-004)
     * 요청 body: employeeId(필수) + name/organizationId/positionId/hireDate(선택)
     * 성공 응답: { "success": true, "data": { "employeeId": 1, "updatedAt": "..." } }
     */
    @PostMapping("/update")
    public ApiResponseDto<EmployeeUpdateResponseDto> updateEmployee(@Valid @RequestBody EmployeeUpdateRequestDto request) {
        Employee updated = employeeService.updateEmployee(request);
        return new ApiResponseDto<>(EmployeeUpdateResponseDto.from(updated));
    }

}
