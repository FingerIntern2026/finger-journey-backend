// EmployeeController
// 역할: HTTP 요청을 받아서 EmployeeService를 호출하고, 결과를 반환하는 계층
// 실제 비즈니스 로직(DB 조회/저장)은 여기 두지 않고 Service에 위임할 것
//
// 응답 형식 (API 명세서 기준):
// - 성공 시: 감싸지 않고 데이터 그대로 반환 (HTTP 200)
// - 실패 시: { "code": "...", "message": "..." } 형태로 반환 (HTTP 4xx/5xx, GlobalExceptionHandler가 처리)
//
// API 명세서 기준 담당 분배:
// - POST /admin/employee/list   (목록조회, ADM-001) → 재웅
// - POST /admin/employee/detail (상세조회, ADM-002) → 규원
// - POST /admin/employee/create (등록,   ADM-003) → 지연
// - POST /admin/employee/update (수정,   ADM-004) → 지연
// - POST /admin/employee/delete (삭제,   ADM-005) → 규원

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // 상세조회: POST /admin/employee/detail
    // 요청 body: { "employeeId": 1 }
    // Service가 예외를 던지면 GlobalExceptionHandler가 자동으로 에러 응답 처리
    @PostMapping("/detail")
    public Employee getEmployeeDetail(@RequestBody Map<String, Long> request) {
        Long employeeId = request.get("employeeId");
        return employeeService.getEmployeeDetail(employeeId);
    }

    // 삭제: POST /admin/employee/delete
    // 요청 body: { "employeeId": 1 }
    // 명세서 기준 성공 시 응답 body 없음 (HTTP 200만 반환)
    @PostMapping("/delete")
    public void deleteEmployee(@RequestBody Map<String, Long> request) {
        Long employeeId = request.get("employeeId");
        employeeService.deleteEmployee(employeeId);
    }
}