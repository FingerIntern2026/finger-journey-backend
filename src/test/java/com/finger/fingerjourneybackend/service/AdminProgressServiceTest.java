package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.ProgressListResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.repository.EmployeeOnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OnboardingStageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProgressServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeOnboardingStageRepository employeeOnboardingStageRepository;
    @Mock
    private OnboardingStageRepository onboardingStageRepository;

    @InjectMocks
    private AdminProgressService adminProgressService;

    @Test
    void progressShowsCompletedStagesOverTotalStages() {
        when(employeeRepository.findAll(any(Sort.class))).thenReturn(List.of(employee(1L, "김신입", "ONBOARDING")));
        when(onboardingStageRepository.count()).thenReturn(9L);
        when(employeeOnboardingStageRepository.countByEmployeeIdAndStatus(1L, "COMPLETED")).thenReturn(3L);

        List<ProgressListResponseDto> response = adminProgressService.getProgressList(null);

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.getEmployeeId()).isEqualTo(1L);
            assertThat(item.getName()).isEqualTo("김신입");
            assertThat(item.getCurrentPhase()).isEqualTo("ONBOARDING");
            assertThat(item.getStageProgress()).isEqualTo("3/9");
        });
    }

    @Test
    void completedEmployeeIsAlwaysFullyProgressed() {
        when(employeeRepository.findAll(any(Sort.class))).thenReturn(List.of(employee(3L, "박기존", "COMPLETED")));
        when(onboardingStageRepository.count()).thenReturn(9L);

        List<ProgressListResponseDto> response = adminProgressService.getProgressList(null);

        assertThat(response).singleElement().extracting(ProgressListResponseDto::getStageProgress).isEqualTo("9/9");
        verify(employeeOnboardingStageRepository, never()).countByEmployeeIdAndStatus(any(), any());
    }

    @Test
    void completedStageCountNeverExceedsTotal() {
        when(employeeRepository.findAll(any(Sort.class))).thenReturn(List.of(employee(1L, "김신입", "ONBOARDING")));
        when(onboardingStageRepository.count()).thenReturn(4L);
        when(employeeOnboardingStageRepository.countByEmployeeIdAndStatus(1L, "COMPLETED")).thenReturn(6L);

        assertThat(adminProgressService.getProgressList(null))
                .singleElement().extracting(ProgressListResponseDto::getStageProgress).isEqualTo("4/4");
    }

    @Test
    void organizationFilterQueriesOnlyThatOrganization() {
        when(employeeRepository.findByOrganizationIdOrderByEmployeeIdAsc(2L))
                .thenReturn(List.of(employee(1L, "김신입", "ONBOARDING")));
        when(onboardingStageRepository.count()).thenReturn(0L);
        when(employeeOnboardingStageRepository.countByEmployeeIdAndStatus(1L, "COMPLETED")).thenReturn(0L);

        List<ProgressListResponseDto> response = adminProgressService.getProgressList(2L);

        assertThat(response).singleElement().extracting(ProgressListResponseDto::getStageProgress).isEqualTo("0/0");
        verify(employeeRepository, never()).findAll(any(Sort.class));
    }

    private Employee employee(Long employeeId, String name, String currentPhase) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setName(name);
        employee.setCurrentPhase(currentPhase);
        return employee;
    }
}
