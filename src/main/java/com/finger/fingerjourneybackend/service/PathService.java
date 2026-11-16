package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.path.response.PathMapItemResponseDto;
import com.finger.fingerjourneybackend.dto.path.response.PathStageClearResponseDto;
import com.finger.fingerjourneybackend.dto.path.response.PathStageEnterResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.EmployeeOnboardingStage;
import com.finger.fingerjourneybackend.entity.OnboardingStage;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.EmployeeOnboardingStageRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OnboardingStageRepository;
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
@Transactional(readOnly = true)
public class PathService {

    private static final String STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private static final String PHASE_COMPLETED = "COMPLETED";

    private final EmployeeRepository employeeRepository;
    private final OnboardingStageRepository onboardingStageRepository;
    private final EmployeeOnboardingStageRepository
            employeeOnboardingStageRepository;

    // 전체 온보딩 스테이지와 로그인 사원 진행 상태 조회
    public List<PathMapItemResponseDto> getPathMap(
            Long employeeId
    ) {
        validateEmployee(employeeId);

        List<OnboardingStage> stages =
                onboardingStageRepository
                        .findAllByOrderByStageOrderAsc();

        Map<Long, EmployeeOnboardingStage> progressMap =
                getEmployeeProgressMap(employeeId);

        return stages.stream()
                .map(stage -> {
                    EmployeeOnboardingStage progress =
                            progressMap.get(
                                    stage.getOnboardingStageId()
                            );

                    String status = progress == null
                            ? STATUS_NOT_STARTED
                            : progress.getStatus();

                    return toPathMapItemResponse(
                            stage,
                            status
                    );
                })
                .toList();
    }

    // 스테이지 진입한다.
    // 첫 번째 스테이지는 바로 진입 가능
    // 두 번째 이후 스테이지는 이전 스테이지가 완료되어야 함.
    @Transactional
    public PathStageEnterResponseDto enterStage(
            Long employeeId,
            Long stageId
    ) {
        validateEmployee(employeeId);

        OnboardingStage stage = findStage(stageId);

        validatePreviousStageCompleted(
                employeeId,
                stage
        );

        EmployeeOnboardingStage progress =
                employeeOnboardingStageRepository
                        .findByEmployeeIdAndOnboardingStageId(
                                employeeId,
                                stageId
                        )
                        .orElse(null);

        if (progress == null) {
            progress = createProgress(
                    employeeId,
                    stageId
            );
        } else if (STATUS_NOT_STARTED.equals(
                progress.getStatus()
        )) {
            progress.setStatus(STATUS_IN_PROGRESS);
            progress.setStartedAt(LocalDateTime.now());

            progress =
                    employeeOnboardingStageRepository.save(
                            progress
                    );
        }

        return PathStageEnterResponseDto.builder()
                .allowed(true)
                .status(progress.getStatus())
                .build();
    }

    // 진입한 스테이지를 완료 상태로 변경.
    @Transactional
    public PathStageClearResponseDto clearStage(
            Long employeeId,
            Long stageId
    ) {
        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.EMPLOYEE_NOT_FOUND
                        )
                );

        findStage(stageId);

        EmployeeOnboardingStage progress =
                employeeOnboardingStageRepository
                        .findByEmployeeIdAndOnboardingStageId(
                                employeeId,
                                stageId
                        )
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.STAGE_NOT_ACCESSIBLE
                                )
                        );

        if (!STATUS_IN_PROGRESS.equals(
                progress.getStatus()
        )) {
            throw new CustomException(
                    ErrorCode.STAGE_NOT_ACCESSIBLE
            );
        }

        LocalDateTime completedAt = LocalDateTime.now();

        progress.setStatus(STATUS_COMPLETED);
        progress.setCompletedAt(completedAt);

        employeeOnboardingStageRepository.save(progress);

        updateEmployeePhaseIfAllCompleted(
                employee,
                employeeId
        );

        return PathStageClearResponseDto.builder()
                .status(progress.getStatus())
                .completedAt(progress.getCompletedAt())
                .build();
    }

    // 사원의 스테이지 진행 기록 조회 후, 스테이지 ID를 키로 하는 맵으로 반환.
    private Map<Long, EmployeeOnboardingStage>
    getEmployeeProgressMap(
            Long employeeId
    ) {
        return employeeOnboardingStageRepository
                .findByEmployeeId(employeeId)
                .stream()
                .collect(Collectors.toMap(
                        EmployeeOnboardingStage
                                ::getOnboardingStageId,
                        Function.identity()
                ));
    }

    // 스테이지 정보와 진행 상태를 맵 조회 응답 DTO로 변환.
    private PathMapItemResponseDto toPathMapItemResponse(
            OnboardingStage stage,
            String status
    ) {
        return PathMapItemResponseDto.builder()
                .stageId(stage.getOnboardingStageId())
                .stageName(stage.getStageName())
                .stageOrder(stage.getStageOrder())
                .status(status)
                .build();
    }

    // 스테이지 최초 진입 기록 생성.
    private EmployeeOnboardingStage createProgress(
            Long employeeId,
            Long stageId
    ) {
        EmployeeOnboardingStage progress =
                new EmployeeOnboardingStage();

        progress.setEmployeeId(employeeId);
        progress.setOnboardingStageId(stageId);
        progress.setStatus(STATUS_IN_PROGRESS);
        progress.setStartedAt(LocalDateTime.now());

        return employeeOnboardingStageRepository.save(
                progress
        );
    }

    // 이전 스테이지가 완료되었는지 확인.
    private void validatePreviousStageCompleted(
            Long employeeId,
            OnboardingStage currentStage
    ) {
        if (currentStage.getStageOrder() <= 1) {
            return;
        }

        Integer previousOrder =
                currentStage.getStageOrder() - 1;

        OnboardingStage previousStage =
                onboardingStageRepository
                        .findByStageOrder(previousOrder)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.STAGE_NOT_FOUND
                                )
                        );

        boolean previousCompleted =
                employeeOnboardingStageRepository
                        .findByEmployeeIdAndOnboardingStageId(
                                employeeId,
                                previousStage
                                        .getOnboardingStageId()
                        )
                        .map(progress ->
                                STATUS_COMPLETED.equals(
                                        progress.getStatus()
                                )
                        )
                        .orElse(false);

        if (!previousCompleted) {
            throw new CustomException(
                    ErrorCode.PREVIOUS_STAGE_NOT_COMPLETED
            );
        }
    }

    // 모든 스테이지 완료 -> 사원의 전체 단계를 COMPLETED로 변경
    private void updateEmployeePhaseIfAllCompleted(
            Employee employee,
            Long employeeId
    ) {
        long totalStageCount =
                onboardingStageRepository.count();

        long completedStageCount =
                employeeOnboardingStageRepository
                        .countByEmployeeIdAndStatus(
                                employeeId,
                                STATUS_COMPLETED
                        );

        if (totalStageCount > 0
                && totalStageCount == completedStageCount) {
            employee.setCurrentPhase(PHASE_COMPLETED);
            employeeRepository.save(employee);
        }
    }

    // 사원 존재 여부 확인 (없으면 예외 발생)
    private void validateEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new CustomException(
                    ErrorCode.EMPLOYEE_NOT_FOUND
            );
        }
    }

    // 스테이지 조회 (없으면 예외 발생)
    private OnboardingStage findStage(Long stageId) {
        return onboardingStageRepository
                .findById(stageId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.STAGE_NOT_FOUND
                        )
                );
    }
}
