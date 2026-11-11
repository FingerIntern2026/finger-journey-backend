package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.bag.response.BagCompleteResponseDto;
import com.finger.fingerjourneybackend.dto.bag.response.BagEnterResponseDto;
import com.finger.fingerjourneybackend.dto.bag.response.BagListItemResponseDto;
import com.finger.fingerjourneybackend.dto.bag.response.BagStatusItemResponseDto;
import com.finger.fingerjourneybackend.entity.BagCheckType;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.EmployeeBagCheck;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.BagCheckTypeRepository;
import com.finger.fingerjourneybackend.repository.EmployeeBagCheckRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.MediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BagServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BagCheckTypeRepository bagCheckTypeRepository;

    @Mock
    private EmployeeBagCheckRepository employeeBagCheckRepository;

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private BagService bagService;

    @Test
    void getBagListReturnsNotStartedWhenEmployeeHasNoRecord() {
        BagCheckType checkType = checkType(1L, "서류 확인", 1);
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(checkType));
        when(employeeBagCheckRepository.findByEmployeeId(1L))
                .thenReturn(List.of());

        List<BagListItemResponseDto> response = bagService.getBagList(1L);

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.getCheckTypeId()).isEqualTo(1L);
            assertThat(item.getCheckName()).isEqualTo("서류 확인");
            assertThat(item.getDisplayOrder()).isEqualTo(1);
            assertThat(item.getStatus()).isEqualTo("NOT_STARTED");
        });
    }

    @Test
    void getBagStatusReturnsCheckedAtFromEmployeeRecord() {
        LocalDateTime checkedAt = LocalDateTime.of(2026, 8, 1, 10, 10);
        BagCheckType checkType = checkType(1L, "서류 확인", 1);
        EmployeeBagCheck bagCheck = bagCheck(1L, "COMPLETED", checkedAt.minusHours(1));
        bagCheck.setCheckedAt(checkedAt);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(checkType));
        when(employeeBagCheckRepository.findByEmployeeId(1L))
                .thenReturn(List.of(bagCheck));

        List<BagStatusItemResponseDto> response = bagService.getBagStatus(1L);

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.getStatus()).isEqualTo("COMPLETED");
            assertThat(item.getCheckedAt()).isEqualTo(checkedAt);
        });
    }

    @Test
    void enterBagItemCreatesInProgressRecordOnFirstEntry() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.existsById(10L)).thenReturn(true);
        when(employeeBagCheckRepository.findByEmployeeIdAndCheckTypeId(1L, 10L))
                .thenReturn(Optional.empty());
        when(employeeBagCheckRepository.save(any(EmployeeBagCheck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BagEnterResponseDto response = bagService.enterBagItem(1L, 10L);

        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getStartedAt()).isNotNull();
        verify(employeeBagCheckRepository).save(any(EmployeeBagCheck.class));
    }

    @Test
    void enterBagItemKeepsOriginalStartedAtOnReentry() {
        LocalDateTime originalStartedAt = LocalDateTime.of(2026, 8, 1, 9, 50);
        EmployeeBagCheck existing = bagCheck(10L, "IN_PROGRESS", originalStartedAt);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.existsById(10L)).thenReturn(true);
        when(employeeBagCheckRepository.findByEmployeeIdAndCheckTypeId(1L, 10L))
                .thenReturn(Optional.of(existing));

        BagEnterResponseDto response = bagService.enterBagItem(1L, 10L);

        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getStartedAt()).isEqualTo(originalStartedAt);
        verify(employeeBagCheckRepository, never()).save(any(EmployeeBagCheck.class));
    }

    @Test
    void enterBagItemDoesNotLowerCompletedStatus() {
        LocalDateTime originalStartedAt = LocalDateTime.of(2026, 8, 1, 9, 50);
        EmployeeBagCheck existing = bagCheck(10L, "COMPLETED", originalStartedAt);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.existsById(10L)).thenReturn(true);
        when(employeeBagCheckRepository.findByEmployeeIdAndCheckTypeId(1L, 10L))
                .thenReturn(Optional.of(existing));

        BagEnterResponseDto response = bagService.enterBagItem(1L, 10L);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getStartedAt()).isEqualTo(originalStartedAt);
        verify(employeeBagCheckRepository, never()).save(any(EmployeeBagCheck.class));
    }

    @Test
    void enterBagItemFailsWhenCheckTypeDoesNotExist() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.existsById(999L)).thenReturn(false);

        assertError(
                () -> bagService.enterBagItem(1L, 999L),
                ErrorCode.BAG_CHECK_ITEM_NOT_FOUND
        );
        verify(employeeBagCheckRepository, never()).save(any(EmployeeBagCheck.class));
    }

    @Test
    void completeBagFailsWhenAnyItemIsIncomplete() {
        Employee employee = employee(1L, "BAG");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(bagCheckTypeRepository.count()).thenReturn(5L);
        when(employeeBagCheckRepository.countByEmployeeIdAndStatus(1L, "COMPLETED"))
                .thenReturn(4L);

        assertError(
                () -> bagService.completeBag(1L),
                ErrorCode.BAG_CHECKLIST_INCOMPLETE
        );
        assertThat(employee.getCurrentPhase()).isEqualTo("BAG");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void completeBagChangesEmployeePhaseToOnboarding() {
        Employee employee = employee(1L, "BAG");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(bagCheckTypeRepository.count()).thenReturn(5L);
        when(employeeBagCheckRepository.countByEmployeeIdAndStatus(1L, "COMPLETED"))
                .thenReturn(5L);
        when(employeeRepository.save(employee)).thenReturn(employee);

        BagCompleteResponseDto response = bagService.completeBag(1L);

        assertThat(response.getCurrentPhase()).isEqualTo("ONBOARDING");
        assertThat(employee.getCurrentPhase()).isEqualTo("ONBOARDING");
        verify(employeeRepository).save(employee);
    }

    private BagCheckType checkType(Long checkTypeId, String checkName, int displayOrder) {
        BagCheckType checkType = new BagCheckType();
        checkType.setCheckTypeId(checkTypeId);
        checkType.setCheckCode("CHECK_" + checkTypeId);
        checkType.setCheckName(checkName);
        checkType.setDisplayOrder(displayOrder);
        return checkType;
    }

    private EmployeeBagCheck bagCheck(
            Long checkTypeId,
            String status,
            LocalDateTime startedAt
    ) {
        EmployeeBagCheck bagCheck = new EmployeeBagCheck();
        bagCheck.setEmployeeId(1L);
        bagCheck.setCheckTypeId(checkTypeId);
        bagCheck.setStatus(status);
        bagCheck.setStartedAt(startedAt);
        return bagCheck;
    }

    private Employee employee(Long employeeId, String currentPhase) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setCurrentPhase(currentPhase);
        return employee;
    }

    private void assertError(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action,
            ErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(expectedErrorCode);
    }
}
