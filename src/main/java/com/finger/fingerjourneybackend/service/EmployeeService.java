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


import com.finger.fingerjourneybackend.dto.EmployeeListResponse;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;

import com.finger.fingerjourneybackend.entity.Position;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
     * - API 명세서 기준: employeeId만 필수, 나머지(name/organizationId/positionId/hireDate)는
     *   전부 선택 항목 → 보낸 값만 부분적으로 수정 (null이면 그대로 유지)
     */
    public Employee updateEmployee(Long employeeId, Employee updatedEmployee) {
        // 1. employeeId로 원본 조회 → 없으면 404 에러
        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMON_404: 해당 직원을 찾을 수 없습니다."));

        // 2. 값이 넘어온 필드만 부분적으로 덮어쓰기 (null이면 원래 값 그대로 둠)
        if (updatedEmployee.getName() != null) {
            existing.setName(updatedEmployee.getName());
        }
        if (updatedEmployee.getOrganizationId() != null) {
            existing.setOrganizationId(updatedEmployee.getOrganizationId());
        }
        if (updatedEmployee.getPositionId() != null) {
            existing.setPositionId(updatedEmployee.getPositionId());
        }
        if (updatedEmployee.getHireDate() != null) {
            existing.setHireDate(updatedEmployee.getHireDate());
        }

        // 3. 저장 (employeeId가 이미 있으므로 덮어쓰기 = update)
        return employeeRepository.save(existing);
    }

    /**
     * 직원 상세조회
     * - employeeId로 조회
     * - 대상이 없으면 COMMON_404 에러
     */
    public Employee getEmployeeDetail(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMON_404: 해당 직원을 찾을 수 없습니다."));
    }

    /**
     * 직원 삭제
     * - employeeId로 대상 존재 여부 먼저 확인
     * - 대상이 없으면 COMMON_404 에러
     * - deleteById는 대상이 없어도 조용히 넘어가는 특성이 있어서, existsById로 먼저 검증함
     */
    public void deleteEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMON_404: 삭제할 직원을 찾을 수 없습니다.");
        }
        employeeRepository.deleteById(employeeId);
    }

}
