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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BagService {

    private static final String STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String PHASE_ONBOARDING = "ONBOARDING";

    private final EmployeeRepository employeeRepository;
    private final BagCheckTypeRepository bagCheckTypeRepository;
    private final EmployeeBagCheckRepository employeeBagCheckRepository;
    private final MediaRepository mediaRepository;

    /**
     * 가방싸기 전체 항목 조회
     *
     * 전체 가방싸기 항목과 로그인 사원의 진행 상태를 함께 반환한다.
     * 사원별 진행 기록이 없는 항목은 NOT_STARTED로 반환한다.
     */
    public List<BagListItemResponseDto> getBagList(Long employeeId) {
        validateEmployee(employeeId);

        List<BagCheckType> checkTypes =
                bagCheckTypeRepository.findAllByOrderByDisplayOrderAsc();

        Map<Long, EmployeeBagCheck> bagCheckByTypeId =
                getEmployeeBagCheckMap(employeeId);

        return checkTypes.stream()
                .map(checkType -> {
                    EmployeeBagCheck employeeBagCheck =
                            bagCheckByTypeId.get(
                                    checkType.getCheckTypeId()
                            );

                    String status = employeeBagCheck == null
                            ? STATUS_NOT_STARTED
                            : employeeBagCheck.getStatus();

                    String iconMediaUrl =
                            findMediaUrl(checkType.getMediaId());

                    return BagListItemResponseDto.builder()
                            .checkTypeId(checkType.getCheckTypeId())
                            .checkName(checkType.getCheckName())
                            .iconMediaUrl(iconMediaUrl)
                            .displayOrder(checkType.getDisplayOrder())
                            .status(status)
                            .build();
                })
                .toList();
    }

    /**
     * 가방싸기 상태 폴링
     *
     * 관리자의 확인 결과가 반영됐는지 확인하기 위해
     * 로그인 사원의 항목별 상태와 확인 시각을 반환한다.
     */
    public List<BagStatusItemResponseDto> getBagStatus(
            Long employeeId
    ) {
        validateEmployee(employeeId);

        List<BagCheckType> checkTypes =
                bagCheckTypeRepository.findAllByOrderByDisplayOrderAsc();

        Map<Long, EmployeeBagCheck> bagCheckByTypeId =
                getEmployeeBagCheckMap(employeeId);

        return checkTypes.stream()
                .map(checkType -> {
                    EmployeeBagCheck employeeBagCheck =
                            bagCheckByTypeId.get(
                                    checkType.getCheckTypeId()
                            );

                    String status = employeeBagCheck == null
                            ? STATUS_NOT_STARTED
                            : employeeBagCheck.getStatus();

                    LocalDateTime checkedAt =
                            employeeBagCheck == null
                                    ? null
                                    : employeeBagCheck.getCheckedAt();

                    return BagStatusItemResponseDto.builder()
                            .checkTypeId(checkType.getCheckTypeId())
                            .status(status)
                            .checkedAt(checkedAt)
                            .build();
                })
                .toList();
    }

    /**
     * 가방싸기 항목 진입
     *
     * 항목에 처음 진입하면 EmployeeBagCheck를 생성하고
     * 상태를 IN_PROGRESS로 변경한다.
     *
     * 재진입한 경우 기존 상태와 최초 진입 시각을 유지한다.
     * 이미 완료된 항목도 IN_PROGRESS로 되돌리지 않는다.
     */
    @Transactional
    public BagEnterResponseDto enterBagItem(
            Long employeeId,
            Long checkTypeId
    ) {
        validateEmployee(employeeId);
        validateBagCheckType(checkTypeId);

        EmployeeBagCheck bagCheck =
                employeeBagCheckRepository
                        .findByEmployeeIdAndCheckTypeId(
                                employeeId,
                                checkTypeId
                        )
                        .orElse(null);

        if (bagCheck == null) {
            bagCheck = createEmployeeBagCheck(
                    employeeId,
                    checkTypeId
            );
        } else if (STATUS_NOT_STARTED.equals(bagCheck.getStatus())) {
            bagCheck.setStatus(STATUS_IN_PROGRESS);

            if (bagCheck.getStartedAt() == null) {
                bagCheck.setStartedAt(LocalDateTime.now());
            }

            bagCheck =
                    employeeBagCheckRepository.save(bagCheck);
        }

        return BagEnterResponseDto.builder()
                .status(bagCheck.getStatus())
                .startedAt(bagCheck.getStartedAt())
                .build();
    }

    /**
     * 가방싸기 완료 처리
     *
     * 전체 가방싸기 항목이 COMPLETED인 경우에만
     * 사원의 현재 단계를 ONBOARDING으로 변경한다.
     */
    @Transactional
    public BagCompleteResponseDto completeBag(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.EMPLOYEE_NOT_FOUND
                        )
                );

        long totalCount = bagCheckTypeRepository.count();

        long completedCount =
                employeeBagCheckRepository
                        .countByEmployeeIdAndStatus(
                                employeeId,
                                STATUS_COMPLETED
                        );

        if (totalCount == 0 || completedCount != totalCount) {
            throw new CustomException(
                    ErrorCode.BAG_CHECKLIST_INCOMPLETE
            );
        }

        employee.setCurrentPhase(PHASE_ONBOARDING);
        employeeRepository.save(employee);

        return BagCompleteResponseDto.builder()
                .currentPhase(employee.getCurrentPhase())
                .build();
    }

    /**
     * 로그인 사원의 가방싸기 진행 기록을
     * checkTypeId를 키로 사용하는 Map으로 변환한다.
     */
    private Map<Long, EmployeeBagCheck> getEmployeeBagCheckMap(
            Long employeeId
    ) {
        return employeeBagCheckRepository
                .findByEmployeeId(employeeId)
                .stream()
                .collect(Collectors.toMap(
                        EmployeeBagCheck::getCheckTypeId,
                        Function.identity()
                ));
    }

    /**
     * 최초 진입한 가방싸기 항목의 진행 기록을 생성한다.
     */
    private EmployeeBagCheck createEmployeeBagCheck(
            Long employeeId,
            Long checkTypeId
    ) {
        EmployeeBagCheck bagCheck = new EmployeeBagCheck();

        bagCheck.setEmployeeId(employeeId);
        bagCheck.setCheckTypeId(checkTypeId);
        bagCheck.setStatus(STATUS_IN_PROGRESS);
        bagCheck.setStartedAt(LocalDateTime.now());

        return employeeBagCheckRepository.save(bagCheck);
    }

    /**
     * 사원 존재 여부를 확인한다.
     */
    private void validateEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new CustomException(
                    ErrorCode.EMPLOYEE_NOT_FOUND
            );
        }
    }

    /**
     * 가방싸기 체크 항목 존재 여부를 확인한다.
     */
    private void validateBagCheckType(Long checkTypeId) {
        if (!bagCheckTypeRepository.existsById(checkTypeId)) {
            throw new CustomException(
                    ErrorCode.BAG_CHECK_ITEM_NOT_FOUND
            );
        }
    }

    /**
     * 가방싸기 항목의 미디어 URL을 조회한다.
     *
     * mediaId가 없거나 연결된 Media가 없으면 null을 반환한다.
     */
    private String findMediaUrl(Long mediaId) {
        if (mediaId == null) {
            return null;
        }

        return mediaRepository.findById(mediaId)
                .map(media -> media.getFileUrl())
                .orElse(null);
    }
}