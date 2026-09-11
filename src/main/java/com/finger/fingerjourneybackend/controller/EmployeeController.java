// EmployeeController
// 역할: HTTP 요청을 받아서 EmployeeService를 호출하고, 결과를 반환하는 계층
// 실제 비즈니스 로직(DB 조회/저장)은 여기 두지 않고 Service에 위임할 것
//
// 응답 형식 (API 명세서 기준):
// - 성공 시: ApiResponse로 감싸서 { "success": true, "data": {...} } 형태로 반환 (HTTP 200)
//   (단, 삭제처럼 명세서상 Body 없음이 명시된 API는 감싸지 않고 void 반환)
// - 실패 시: { "success": false, "code": "...", "message": "..." } 형태로 반환
//   (예외는 exception/GlobalExceptionHandler.java 에서 전역으로 처리함)

package com.finger.fingerjourneybackend.controller;


import com.finger.fingerjourneybackend.dto.admin.request.EmployeeCreateRequest;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeUpdateRequest;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeCreateResponse;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeUpdateResponse;
import com.finger.fingerjourneybackend.dto.ApiResponse;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeIdRequest;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeDetailResponse;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeListResponse;
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
    public ApiResponse<List<EmployeeListResponse>> getEmployeeList() {
        return new ApiResponse<>(employeeService.getEmployeeList());
    }

    // 상세조회: POST /admin/employee/detail
    // 요청 body: { "employeeId": 1 }
    // @Valid: EmployeeIdRequest의 @NotNull 검증을 여기서 실제로 수행함
    @PostMapping("/detail")
    public ApiResponse<EmployeeDetailResponse> getEmployeeDetail(@Valid @RequestBody EmployeeIdRequest request) {
        Employee employee = employeeService.getEmployeeDetail(request.getEmployeeId());
        return new ApiResponse<>(EmployeeDetailResponse.from(employee));
    }

    // 삭제: POST /admin/employee/delete
    // 요청 body: { "employeeId": 1 }
    // 명세서 기준 성공 시 응답 body 없음 (HTTP 200만 반환)
    @PostMapping("/delete")
    public void deleteEmployee(@Valid @RequestBody EmployeeIdRequest request) {
        employeeService.deleteEmployee(request.getEmployeeId());
    }


    /**
     * 사원 등록 
     * 요청 body: employeeNo, name, organizationId, positionId, hireDate
     * 성공 응답: { "success": true, "data": { "employeeId": 5 } }
     */
    @PostMapping("/create")
    public ApiResponse<EmployeeCreateResponse> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        Employee saved = employeeService.createEmployee(request);
        return new ApiResponse<>(EmployeeCreateResponse.from(saved));
 
    }

    /**
     * 사원 수정 (지연 담당, ADM-004)
     * 요청 body: employeeId(필수) + name/organizationId/positionId/hireDate(선택)
     * 성공 응답: { "success": true, "data": { "employeeId": 1, "updatedAt": "..." } }
     */
    @PostMapping("/update")
    public ApiResponse<EmployeeUpdateResponse> updateEmployee(@Valid @RequestBody EmployeeUpdateRequest request) {
        Employee updated = employeeService.updateEmployee(request);
        return new ApiResponse<>(EmployeeUpdateResponse.from(updated));
    }

}    