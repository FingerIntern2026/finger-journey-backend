// EmployeeService
// 역할: 실제 비즈니스 로직 담당 (Controller와 Repository 사이 계층)
// EmployeeRepository를 주입받아서 사용, 예외 처리/데이터 가공 등은 여기서 함

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.request.EmployeeCreateRequest;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeUpdateRequest;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeListResponse;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;

import com.finger.fingerjourneybackend.entity.Position;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor


public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final PositionRepository positionRepository;

    public List<EmployeeListResponse> getEmployeeList() {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .map(this::toListResponse)
                .toList();
    }

    private EmployeeListResponse toListResponse(Employee employee) {
        String organizationName = organizationRepository.findById(employee.getOrganizationId())
                .map(Organization::getOrganizationName)
                .orElse(null);

        String positionName = positionRepository.findById(employee.getPositionId())
                .map(Position::getPositionName)
                .orElse(null);

        Integer progressRate = "COMPLETED".equals(employee.getCurrentPhase()) ? null : 0;

        return EmployeeListResponse.builder()
                .employeeId(employee.getEmployeeId())
                .employeeNo(employee.getEmployeeNo())
                .name(employee.getName())
                .organizationName(organizationName)
                .positionName(positionName)
                .currentPhase(employee.getCurrentPhase())
                .progressRate(progressRate)
                .build();
    }





    /**
     * 직원 등록
     * - employeeNo(사번)가 이미 있으면 EMPLOYEE_NUMBER_DUPLICATE 에러
     * - currentPhase는 무조건 "PREBOARDING"으로 고정 (요청값 무시)
     */
    public Employee createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new CustomException(ErrorCode.EMPLOYEE_NUMBER_DUPLICATE);
        }

        Employee employee = new Employee();
        employee.setEmployeeNo(request.getEmployeeNo());
        employee.setName(request.getName());
        employee.setOrganizationId(request.getOrganizationId());
        employee.setPositionId(request.getPositionId());
        employee.setHireDate(request.getHireDate());
        employee.setCurrentPhase("PREBOARDING");

        return employeeRepository.save(employee);
    }

    /**
     * 직원 정보 수정
     * - 대상이 없으면 EMPLOYEE_NOT_FOUND 에러
     * - API 명세서 기준: employeeId만 필수, 나머지(name/organizationId/positionId/hireDate)는
     *   전부 선택 항목 → 보낸 값만 부분적으로 수정 (null이면 그대로 유지)
     */
    public Employee updateEmployee(EmployeeUpdateRequest request) {
        Employee existing = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getOrganizationId() != null) {
            existing.setOrganizationId(request.getOrganizationId());
        }
        if (request.getPositionId() != null) {
            existing.setPositionId(request.getPositionId());
        }
        if (request.getHireDate() != null) {
            existing.setHireDate(request.getHireDate());
        }

        return employeeRepository.save(existing);
    }

    /**
     * 직원 상세조회
     * - employeeId로 조회
     * - 대상이 없으면 EMPLOYEE_NOT_FOUND 에러
     */
    public Employee getEmployeeDetail(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    /**
     * 직원 삭제
     * - employeeId로 대상 존재 여부 먼저 확인
     * - 대상이 없으면 EMPLOYEE_NOT_FOUND 에러
     * - deleteById는 대상이 없어도 조용히 넘어가는 특성이 있어서, existsById로 먼저 검증함
     */
    public void deleteEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
        employeeRepository.deleteById(employeeId);
    }

}