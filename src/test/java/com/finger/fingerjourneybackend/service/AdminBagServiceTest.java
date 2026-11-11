package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.BagChecklistResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.BagConfirmResponseDto;
import com.finger.fingerjourneybackend.entity.BagCheckType;
import com.finger.fingerjourneybackend.entity.EmployeeBagCheck;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.BagCheckTypeRepository;
import com.finger.fingerjourneybackend.repository.EmployeeBagCheckRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
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
class AdminBagServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BagCheckTypeRepository bagCheckTypeRepository;
    @Mock
    private EmployeeBagCheckRepository employeeBagCheckRepository;

    @InjectMocks
    private AdminBagService adminBagService;

    @Test
    void checklistMarksUnenteredItemsAsNotStarted() {
        LocalDateTime checkedAt = LocalDateTime.of(2026, 11, 11, 10, 0);
        EmployeeBagCheck completed = bagCheck(1L, "COMPLETED");
        completed.setCheckedAt(checkedAt);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(checkType(1L, "서류 확인"), checkType(2L, "업무 장비 확인")));
        when(employeeBagCheckRepository.findByEmployeeId(1L)).thenReturn(List.of(completed));

        List<BagChecklistResponseDto> response = adminBagService.getChecklist(1L);

        assertThat(response).extracting(BagChecklistResponseDto::getStatus)
                .containsExactly("COMPLETED", "NOT_STARTED");
        assertThat(response).extracting(BagChecklistResponseDto::getCheckName)
                .containsExactly("서류 확인", "업무 장비 확인");
        assertThat(response.get(0).getCheckedAt()).isEqualTo(checkedAt);
        assertThat(response.get(1).getCheckedAt()).isNull();
    }

    @Test
    void checklistFailsWhenEmployeeDoesNotExist() {
        when(employeeRepository.existsById(999L)).thenReturn(false);

        assertError(() -> adminBagService.getChecklist(999L), ErrorCode.EMPLOYEE_NOT_FOUND);
    }

    @Test
    void confirmCompletesInProgressItemAndRecordsAdmin() {
        EmployeeBagCheck inProgress = bagCheck(1L, "IN_PROGRESS");
        stubExistingItem(inProgress);
        when(employeeBagCheckRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BagConfirmResponseDto response = adminBagService.confirm(1L, 1L, 77L);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getCheckedAt()).isNotNull();
        assertThat(inProgress.getCheckedBy()).isEqualTo(77L);
        assertThat(inProgress.getCheckedAt()).isEqualTo(response.getCheckedAt());
    }

    @Test
    void confirmKeepsExistingRecordWhenAlreadyCompleted() {
        LocalDateTime firstCheckedAt = LocalDateTime.of(2026, 11, 10, 9, 0);
        EmployeeBagCheck completed = bagCheck(1L, "COMPLETED");
        completed.setCheckedBy(5L);
        completed.setCheckedAt(firstCheckedAt);
        stubExistingItem(completed);

        BagConfirmResponseDto response = adminBagService.confirm(1L, 1L, 77L);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getCheckedAt()).isEqualTo(firstCheckedAt);
        assertThat(completed.getCheckedBy()).isEqualTo(5L);
        verify(employeeBagCheckRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmFailsWhenEmployeeHasNotEnteredTheItem() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.existsById(1L)).thenReturn(true);
        when(employeeBagCheckRepository.findByEmployeeIdAndCheckTypeId(1L, 1L)).thenReturn(Optional.empty());

        assertError(() -> adminBagService.confirm(1L, 1L, 77L), ErrorCode.RESOURCE_CONFLICT);
        verify(employeeBagCheckRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmFailsWhenItemIsStillNotStarted() {
        stubExistingItem(bagCheck(1L, "NOT_STARTED"));

        assertError(() -> adminBagService.confirm(1L, 1L, 77L), ErrorCode.RESOURCE_CONFLICT);
    }

    @Test
    void confirmFailsWhenEmployeeDoesNotExist() {
        when(employeeRepository.existsById(999L)).thenReturn(false);

        assertError(() -> adminBagService.confirm(999L, 1L, 77L), ErrorCode.EMPLOYEE_NOT_FOUND);
    }

    @Test
    void confirmFailsWhenCheckTypeDoesNotExist() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.existsById(999L)).thenReturn(false);

        assertError(() -> adminBagService.confirm(1L, 999L, 77L), ErrorCode.BAG_CHECK_ITEM_NOT_FOUND);
    }

    private void stubExistingItem(EmployeeBagCheck bagCheck) {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(bagCheckTypeRepository.existsById(1L)).thenReturn(true);
        when(employeeBagCheckRepository.findByEmployeeIdAndCheckTypeId(1L, 1L)).thenReturn(Optional.of(bagCheck));
    }

    private BagCheckType checkType(Long checkTypeId, String checkName) {
        BagCheckType checkType = new BagCheckType();
        checkType.setCheckTypeId(checkTypeId);
        checkType.setCheckName(checkName);
        return checkType;
    }

    private EmployeeBagCheck bagCheck(Long checkTypeId, String status) {
        EmployeeBagCheck bagCheck = new EmployeeBagCheck();
        bagCheck.setEmployeeId(1L);
        bagCheck.setCheckTypeId(checkTypeId);
        bagCheck.setStatus(status);
        return bagCheck;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
