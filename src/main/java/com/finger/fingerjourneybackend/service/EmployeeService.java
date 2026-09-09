// EmployeeService
// 역할: 실제 비즈니스 로직 담당 (Controller와 Repository 사이 계층)
// EmployeeRepository를 주입받아서 사용, 예외 처리/데이터 가공 등은 여기서 함
//
// 담당 분배:
// - 목록조회 로직 → 재웅
// - 상세조회/삭제 로직 → 규원 (상세조회 시 대상 없으면 COMMON_404 처리 필요)
// - 등록/수정 로직 → 지연 (등록 시 employeeNo 중복이면 ADM_003 에러 처리,
//                        생성 시 currentPhase는 "PREBOARDING"으로 고정)

package com.finger.fingerjourneybackend.service;


import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> getEmployeeList() {
        return employeeRepository.findAll();
    }
        /**
     * 직원 등록 
     * - employeeNo(사번)가 이미 있으면 ADM_003 에러
     * - currentPhase는 무조건 "PREBOARDING"으로 고정 (요청값 무시)
     */
    public Employee createEmployee(Employee employee) {
        // 1. 사번 중복 체크 → 있으면 에러 던지고 여기서 멈춤
        if (employeeRepository.existsByEmployeeNo(employee.getEmployeeNo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ADM_003: 이미 존재하는 사번입니다.");
        }

        // 2. 온보딩 단계는 등록 시 항상 첫 단계로 고정
        employee.setCurrentPhase("PREBOARDING");

        // 3. 저장 (employeeId가 없으므로 새로 등록됨 = insert)
        return employeeRepository.save(employee);
    }

    /**
     * 직원 정보 수정 
     * - 대상이 없으면 COMMON_404 에러
     * - employeeNo, currentPhase는 이 메서드에서 수정 대상 아님
     */
    public Employee updateEmployee(Long employeeId, Employee updatedEmployee) {
        // 1. employeeId로 원본 조회 → 없으면 404 에러
        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMON_404: 해당 직원을 찾을 수 없습니다."));

        // 2. 새로 받은 값들로 원본 덮어쓰기 (이름/부서/직급/입사일만)
        existing.setName(updatedEmployee.getName());
        existing.setOrganizationId(updatedEmployee.getOrganizationId());
        existing.setPositionId(updatedEmployee.getPositionId());
        existing.setHireDate(updatedEmployee.getHireDate());

        // 3. 저장 (employeeId가 이미 있으므로 덮어쓰기 = update)
        return employeeRepository.save(existing);
    }

}
