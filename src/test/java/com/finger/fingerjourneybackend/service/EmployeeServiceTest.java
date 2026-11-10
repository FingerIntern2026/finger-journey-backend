package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.EmployeeDetailResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeListResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.entity.Position;
import com.finger.fingerjourneybackend.repository.EmployeeOnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private EmployeeOnboardingStageRepository employeeOnboardingStageRepository;
    @Mock
    private OnboardingStageRepository onboardingStageRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void getEmployeeDetailReturnsOrganizationAndPositionNames() {
        Employee employee = employee(1L, "ONBOARDING");
        Organization organization = new Organization();
        organization.setOrganizationName("개발1팀");
        Position position = new Position();
        position.setPositionName("사원");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
        when(positionRepository.findById(20L)).thenReturn(Optional.of(position));

        EmployeeDetailResponseDto response = employeeService.getEmployeeDetail(1L);

        assertThat(response.getOrganizationName()).isEqualTo("개발1팀");
        assertThat(response.getPositionName()).isEqualTo("사원");
    }

    @Test
    void getEmployeeListCalculatesProgressFromCompletedStages() {
        Employee employee = employee(1L, "ONBOARDING");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(onboardingStageRepository.count()).thenReturn(4L);
        when(employeeOnboardingStageRepository.countByEmployeeIdAndStatus(1L, "COMPLETED"))
                .thenReturn(2L);

        List<EmployeeListResponseDto> response = employeeService.getEmployeeList();

        assertThat(response).singleElement()
                .extracting(EmployeeListResponseDto::getProgressRate)
                .isEqualTo(50);
    }

    @Test
    void completedEmployeeAlwaysHasOneHundredPercentProgress() {
        Employee employee = employee(1L, "COMPLETED");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<EmployeeListResponseDto> response = employeeService.getEmployeeList();

        assertThat(response).singleElement()
                .extracting(EmployeeListResponseDto::getProgressRate)
                .isEqualTo(100);
    }

    private Employee employee(Long employeeId, String currentPhase) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setEmployeeNo("12609001");
        employee.setName("김신입");
        employee.setOrganizationId(10L);
        employee.setPositionId(20L);
        employee.setHireDate(LocalDate.of(2026, 8, 1));
        employee.setCurrentPhase(currentPhase);
        return employee;
    }
}
