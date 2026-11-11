// AdminProgressService
// 역할: 입사자별 온보딩 진행현황 조회 (API-ADM-008)

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.ProgressListResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.repository.EmployeeOnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OnboardingStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProgressService {

    private static final String COMPLETED = "COMPLETED";

    private final EmployeeRepository employeeRepository;
    private final EmployeeOnboardingStageRepository employeeOnboardingStageRepository;
    private final OnboardingStageRepository onboardingStageRepository;

    /**
     * 진행현황 조회
     * - organizationId가 있으면 해당 조직 사원만, 없으면 전체 사원을 사원 ID 순으로 반환
     * - stageProgress는 "완료한 스테이지 수/전체 스테이지 수" (예: 3/9)
     */
    public List<ProgressListResponseDto> getProgressList(Long organizationId) {
        List<Employee> employees = organizationId == null
                ? employeeRepository.findAll(Sort.by("employeeId"))
                : employeeRepository.findByOrganizationIdOrderByEmployeeIdAsc(organizationId);

        long totalStageCount = onboardingStageRepository.count();

        return employees.stream()
                .map(employee -> toResponse(employee, totalStageCount))
                .toList();
    }

    private ProgressListResponseDto toResponse(Employee employee, long totalStageCount) {
        // 사원 목록의 진행률과 같은 기준: 완주한 사원은 항상 전체 완료로 본다.
        long completedStageCount = COMPLETED.equals(employee.getCurrentPhase())
                ? totalStageCount
                : Math.min(
                        employeeOnboardingStageRepository.countByEmployeeIdAndStatus(employee.getEmployeeId(), COMPLETED),
                        totalStageCount);

        return ProgressListResponseDto.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .currentPhase(employee.getCurrentPhase())
                .stageProgress(completedStageCount + "/" + totalStageCount)
                .build();
    }
}
