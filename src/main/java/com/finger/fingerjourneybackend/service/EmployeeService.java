// EmployeeService
// 역할: 실제 비즈니스 로직 담당 (Controller와 Repository 사이 계층)
// EmployeeRepository를 주입받아서 사용, 예외 처리/데이터 가공 등은 여기서 함

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.request.EmployeeCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeDetailResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeListResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.repository.AiReportRepository;
import com.finger.fingerjourneybackend.repository.EmployeeBagCheckRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.QuizResponseRepository;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemLineRepository;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemRepository;
import com.finger.fingerjourneybackend.repository.EmployeeOnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.OnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;

import com.finger.fingerjourneybackend.entity.Position;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor


public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOnboardingStageRepository employeeOnboardingStageRepository;
    private final OnboardingStageRepository onboardingStageRepository;
    // 사원 삭제 시 함께 지우는 연관 데이터 (API-ADM-005)
    private final ThreeLinePoemRepository threeLinePoemRepository;
    private final ThreeLinePoemLineRepository threeLinePoemLineRepository;
    private final QuizResponseRepository quizResponseRepository;
    private final AiReportRepository aiReportRepository;
    private final EmployeeBagCheckRepository employeeBagCheckRepository;
    private final AdminRepository adminRepository;

    public List<EmployeeListResponseDto> getEmployeeList() {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .map(this::toListResponse)
                .toList();
    }

    private EmployeeListResponseDto toListResponse(Employee employee) {
        String organizationName = organizationRepository.findById(employee.getOrganizationId())
                .map(Organization::getOrganizationName)
                .orElse(null);

        String positionName = positionRepository.findById(employee.getPositionId())
                .map(Position::getPositionName)
                .orElse(null);

        Integer progressRate = calculateProgressRate(employee);

        return EmployeeListResponseDto.builder()
                .employeeId(employee.getEmployeeId())
                .employeeNo(employee.getEmployeeNo())
                .name(employee.getName())
                .organizationName(organizationName)
                .positionName(positionName)
                .currentPhase(employee.getCurrentPhase())
                .progressRate(progressRate)
                .build();
    }

    private int calculateProgressRate(Employee employee) {
        if ("COMPLETED".equals(employee.getCurrentPhase())) {
            return 100;
        }

        long totalStageCount = onboardingStageRepository.count();
        if (totalStageCount == 0) {
            return 0;
        }

        long completedStageCount = employeeOnboardingStageRepository
                .countByEmployeeIdAndStatus(employee.getEmployeeId(), "COMPLETED");

        long progressRate = completedStageCount * 100 / totalStageCount;
        return (int) Math.min(progressRate, 100);
    }





    /**
     * 직원 등록
     * - employeeNo(사번)가 이미 있으면 EMPLOYEE_NUMBER_DUPLICATE 에러
     * - currentPhase는 무조건 "PREBOARDING"으로 고정 (요청값 무시)
     */
    public Employee createEmployee(EmployeeCreateRequestDto request) {
        if (employeeRepository.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new CustomException(ErrorCode.EMPLOYEE_NUMBER_DUPLICATE);
        }

        Employee employee = new Employee();
        employee.setEmployeeNo(request.getEmployeeNo());
        employee.setName(request.getName());
        employee.setOrganizationId(request.getOrganizationId());
        employee.setPositionId(request.getPositionId());
        employee.setHireDate(request.getHireDate());
        employee.setBirthDate(request.getBirthDate());
        employee.setCurrentPhase("PREBOARDING");

        return employeeRepository.save(employee);
    }

    /**
     * 직원 정보 수정
     * - 대상이 없으면 EMPLOYEE_NOT_FOUND 에러
     * - API 명세서 기준: employeeId만 필수, 나머지(name/organizationId/positionId/hireDate/birthDate)는
     *   전부 선택 항목 → 보낸 값만 부분적으로 수정 (null이면 그대로 유지)
     */
    public Employee updateEmployee(EmployeeUpdateRequestDto request) {
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
        if (request.getBirthDate() != null) {
            existing.setBirthDate(request.getBirthDate());
        }

        return employeeRepository.save(existing);
    }

    /**
     * 직원 상세조회
     * - employeeId로 조회
     * - 대상이 없으면 EMPLOYEE_NOT_FOUND 에러
     */
    public EmployeeDetailResponseDto getEmployeeDetail(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        String organizationName = organizationRepository.findById(employee.getOrganizationId())
                .map(Organization::getOrganizationName)
                .orElse(null);
        String positionName = positionRepository.findById(employee.getPositionId())
                .map(Position::getPositionName)
                .orElse(null);

        return EmployeeDetailResponseDto.from(employee, organizationName, positionName);
    }

    /**
     * 직원 삭제
     * - employeeId로 대상 존재 여부 먼저 확인
     * - 대상이 없으면 EMPLOYEE_NOT_FOUND 에러
     * - deleteById는 대상이 없어도 조용히 넘어가는 특성이 있어서, existsById로 먼저 검증함
     * - 사원에게 딸린 데이터(3행시, 퀴즈 응답, AI 리포트, 스테이지 진행, 가방싸기, 관리자 권한)를
     *   사원보다 먼저 지우고, 한 트랜잭션이라 중간에 실패하면 모두 되돌린다.
     */
    @Transactional
    public void deleteEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        threeLinePoemRepository.findByEmployeeId(employeeId)
                .ifPresent(poem -> threeLinePoemLineRepository.deleteByPoemId(poem.getPoemId()));
        threeLinePoemRepository.deleteByEmployeeId(employeeId);
        quizResponseRepository.deleteByEmployeeId(employeeId);
        aiReportRepository.deleteByEmployeeId(employeeId);
        employeeOnboardingStageRepository.deleteByEmployeeId(employeeId);
        employeeBagCheckRepository.deleteByEmployeeId(employeeId);
        adminRepository.deleteByEmployeeId(employeeId);

        employeeRepository.deleteById(employeeId);
    }

}
