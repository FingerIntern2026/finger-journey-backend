package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.EmployeeDetailResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.EmployeeListResponseDto;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.EmployeeUpdateRequestDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.entity.Position;
import com.finger.fingerjourneybackend.entity.ThreeLinePoem;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.repository.AiReportRepository;
import com.finger.fingerjourneybackend.repository.EmployeeBagCheckRepository;
import com.finger.fingerjourneybackend.repository.QuizResponseRepository;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemLineRepository;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemRepository;
import com.finger.fingerjourneybackend.repository.EmployeeOnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @Mock
    private ThreeLinePoemRepository threeLinePoemRepository;
    @Mock
    private ThreeLinePoemLineRepository threeLinePoemLineRepository;
    @Mock
    private QuizResponseRepository quizResponseRepository;
    @Mock
    private AiReportRepository aiReportRepository;
    @Mock
    private EmployeeBagCheckRepository employeeBagCheckRepository;
    @Mock
    private AdminRepository adminRepository;

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

    @Test
    void detailFailsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(() -> employeeService.getEmployeeDetail(999L), ErrorCode.EMPLOYEE_NOT_FOUND);
    }

    @Test
    void updateFailsWhenEmployeeDoesNotExist() {
        EmployeeUpdateRequestDto request = new EmployeeUpdateRequestDto();
        ReflectionTestUtils.setField(request, "employeeId", 999L);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(() -> employeeService.updateEmployee(request), ErrorCode.EMPLOYEE_NOT_FOUND);
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteFailsWhenEmployeeDoesNotExist() {
        when(employeeRepository.existsById(999L)).thenReturn(false);

        assertError(() -> employeeService.deleteEmployee(999L), ErrorCode.EMPLOYEE_NOT_FOUND);
        verify(employeeRepository, never()).deleteById(999L);
    }

    @Test
    void createFailsWhenEmployeeNumberIsDuplicated() {
        EmployeeCreateRequestDto request = new EmployeeCreateRequestDto();
        ReflectionTestUtils.setField(request, "employeeNo", "12609001");
        when(employeeRepository.existsByEmployeeNo("12609001")).thenReturn(true);

        assertError(() -> employeeService.createEmployee(request), ErrorCode.EMPLOYEE_NUMBER_DUPLICATE);
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createSavesBirthDate() {
        EmployeeCreateRequestDto request = new EmployeeCreateRequestDto();
        ReflectionTestUtils.setField(request, "employeeNo", "12609010");
        ReflectionTestUtils.setField(request, "name", "신입사원");
        ReflectionTestUtils.setField(request, "organizationId", 10L);
        ReflectionTestUtils.setField(request, "positionId", 20L);
        ReflectionTestUtils.setField(request, "hireDate", LocalDate.of(2026, 11, 1));
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(2000, 3, 15));
        when(employeeRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee saved = employeeService.createEmployee(request);

        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(2000, 3, 15));
        assertThat(saved.getCurrentPhase()).isEqualTo("PREBOARDING");
    }

    @Test
    void updateChangesBirthDateOnlyWhenProvided() {
        Employee employee = employee(1L, "ONBOARDING");
        employee.setBirthDate(LocalDate.of(2000, 3, 15));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeUpdateRequestDto withoutBirthDate = new EmployeeUpdateRequestDto();
        ReflectionTestUtils.setField(withoutBirthDate, "employeeId", 1L);
        ReflectionTestUtils.setField(withoutBirthDate, "name", "김수정");
        assertThat(employeeService.updateEmployee(withoutBirthDate).getBirthDate())
                .isEqualTo(LocalDate.of(2000, 3, 15));

        EmployeeUpdateRequestDto withBirthDate = new EmployeeUpdateRequestDto();
        ReflectionTestUtils.setField(withBirthDate, "employeeId", 1L);
        ReflectionTestUtils.setField(withBirthDate, "birthDate", LocalDate.of(1999, 12, 31));
        assertThat(employeeService.updateEmployee(withBirthDate).getBirthDate())
                .isEqualTo(LocalDate.of(1999, 12, 31));
    }

    @Test
    void deleteRemovesRelatedDataBeforeTheEmployee() {
        ThreeLinePoem poem = new ThreeLinePoem();
        poem.setPoemId(30L);
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(threeLinePoemRepository.findByEmployeeId(1L)).thenReturn(Optional.of(poem));

        employeeService.deleteEmployee(1L);

        InOrder inOrder = inOrder(threeLinePoemLineRepository, threeLinePoemRepository, quizResponseRepository,
                aiReportRepository, employeeOnboardingStageRepository, employeeBagCheckRepository,
                adminRepository, employeeRepository);
        inOrder.verify(threeLinePoemLineRepository).deleteByPoemId(30L);
        inOrder.verify(threeLinePoemRepository).deleteByEmployeeId(1L);
        inOrder.verify(quizResponseRepository).deleteByEmployeeId(1L);
        inOrder.verify(aiReportRepository).deleteByEmployeeId(1L);
        inOrder.verify(employeeOnboardingStageRepository).deleteByEmployeeId(1L);
        inOrder.verify(employeeBagCheckRepository).deleteByEmployeeId(1L);
        inOrder.verify(adminRepository).deleteByEmployeeId(1L);
        inOrder.verify(employeeRepository).deleteById(1L);
    }

    @Test
    void deleteWorksForEmployeeWithoutPoem() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(threeLinePoemRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());

        employeeService.deleteEmployee(1L);

        verify(threeLinePoemLineRepository, never()).deleteByPoemId(org.mockito.ArgumentMatchers.any());
        verify(adminRepository).deleteByEmployeeId(1L);
        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void deleteLeavesRelatedDataAloneWhenEmployeeDoesNotExist() {
        when(employeeRepository.existsById(999L)).thenReturn(false);

        assertError(() -> employeeService.deleteEmployee(999L), ErrorCode.EMPLOYEE_NOT_FOUND);
        verify(adminRepository, never()).deleteByEmployeeId(org.mockito.ArgumentMatchers.any());
        verify(employeeBagCheckRepository, never()).deleteByEmployeeId(org.mockito.ArgumentMatchers.any());
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
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
