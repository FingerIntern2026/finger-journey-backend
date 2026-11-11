// AdminBagService
// 역할: HR 관리 화면의 가방싸기 체크리스트 조회와 확인 처리 (API-ADM-009, 010)
// 입사자용 BagService와 같은 테이블(EmployeeBagCheck)과 상태 값을 사용한다.

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.BagChecklistResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.BagConfirmResponseDto;
import com.finger.fingerjourneybackend.entity.EmployeeBagCheck;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.BagCheckTypeRepository;
import com.finger.fingerjourneybackend.repository.EmployeeBagCheckRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBagService {

    private static final String STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final EmployeeRepository employeeRepository;
    private final BagCheckTypeRepository bagCheckTypeRepository;
    private final EmployeeBagCheckRepository employeeBagCheckRepository;

    /**
     * 선택한 입사자의 항목별 가방싸기 상태 조회
     * - 입사자가 아직 진입하지 않은 항목은 NOT_STARTED로 반환
     */
    public List<BagChecklistResponseDto> getChecklist(Long employeeId) {
        validateEmployee(employeeId);

        Map<Long, EmployeeBagCheck> bagCheckByTypeId = employeeBagCheckRepository
                .findByEmployeeId(employeeId)
                .stream()
                .collect(Collectors.toMap(EmployeeBagCheck::getCheckTypeId, Function.identity()));

        return bagCheckTypeRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(checkType -> {
                    EmployeeBagCheck bagCheck = bagCheckByTypeId.get(checkType.getCheckTypeId());

                    return BagChecklistResponseDto.builder()
                            .checkTypeId(checkType.getCheckTypeId())
                            .checkName(checkType.getCheckName())
                            .status(bagCheck == null ? STATUS_NOT_STARTED : bagCheck.getStatus())
                            .checkedAt(bagCheck == null ? null : bagCheck.getCheckedAt())
                            .build();
                })
                .toList();
    }

    /**
     * 가방싸기 항목 확인 처리 (IN_PROGRESS → COMPLETED)
     * - 확인자(checkedBy)와 확인 일시(checkedAt)를 기록한다.
     * - 이미 COMPLETED면 기존 확인 기록을 바꾸지 않고 그대로 반환한다. (재요청해도 안전)
     * - 입사자가 아직 진입하지 않은 항목은 확인할 수 없어 409로 응답한다.
     *
     * @param adminEmployeeId 확인한 관리자의 사원 ID (권한 검사가 꺼져 있으면 null)
     */
    @Transactional
    public BagConfirmResponseDto confirm(Long employeeId, Long checkTypeId, Long adminEmployeeId) {
        validateEmployee(employeeId);

        if (!bagCheckTypeRepository.existsById(checkTypeId)) {
            throw new CustomException(ErrorCode.BAG_CHECK_ITEM_NOT_FOUND);
        }

        EmployeeBagCheck bagCheck = employeeBagCheckRepository
                .findByEmployeeIdAndCheckTypeId(employeeId, checkTypeId)
                .orElseThrow(this::notEnteredYet);

        if (!STATUS_COMPLETED.equals(bagCheck.getStatus())) {
            if (!STATUS_IN_PROGRESS.equals(bagCheck.getStatus())) {
                throw notEnteredYet();
            }

            bagCheck.setStatus(STATUS_COMPLETED);
            bagCheck.setCheckedBy(adminEmployeeId);
            // DB는 마이크로초까지만 저장하므로 응답과 저장 값이 같도록 미리 맞춘다.
            bagCheck.setCheckedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
            bagCheck = employeeBagCheckRepository.saveAndFlush(bagCheck);
        }

        return BagConfirmResponseDto.builder()
                .status(bagCheck.getStatus())
                .checkedAt(bagCheck.getCheckedAt())
                .build();
    }

    private CustomException notEnteredYet() {
        return new CustomException(ErrorCode.RESOURCE_CONFLICT, "입사자가 아직 진입하지 않은 항목은 확인 처리할 수 없습니다.");
    }

    private void validateEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
    }
}
