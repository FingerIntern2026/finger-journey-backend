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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private OnboardingStageRepository onboardingStageRepository;

    @Mock
    private EmployeeOnboardingStageRepository employeeOnboardingStageRepository;

    @InjectMocks
    private PathService pathService;

    // 진행 기록이 없으면 스테이지 상태를 NOT_STARTED로 반환
    @Test
    void getPathMapReturnsNotStartedWhenProgressDoesNotExist() {
        OnboardingStage stage = stage(1L, "핑거 이야기", 1);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(onboardingStageRepository.findAllByOrderByStageOrderAsc())
                .thenReturn(List.of(stage));
        when(employeeOnboardingStageRepository.findByEmployeeId(1L))
                .thenReturn(List.of());

        List<PathMapItemResponseDto> response = pathService.getPathMap(1L);

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.getStageId()).isEqualTo(1L);
            assertThat(item.getStageName()).isEqualTo("핑거 이야기");
            assertThat(item.getStageOrder()).isEqualTo(1);
            assertThat(item.getStatus()).isEqualTo("NOT_STARTED");
        });
    }

    // 첫 번째 스테이지 최초 진입 시 IN_PROGRESS 기록 생성
    @Test
    void enterFirstStageCreatesInProgressRecord() {
        OnboardingStage firstStage = stage(1L, "핑거 이야기", 1);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(onboardingStageRepository.findById(1L))
                .thenReturn(Optional.of(firstStage));
        when(employeeOnboardingStageRepository
                .findByEmployeeIdAndOnboardingStageId(1L, 1L))
                .thenReturn(Optional.empty());
        when(employeeOnboardingStageRepository.save(any(EmployeeOnboardingStage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PathStageEnterResponseDto response = pathService.enterStage(1L, 1L);

        assertThat(response.getAllowed()).isTrue();
        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        verify(employeeOnboardingStageRepository)
                .save(any(EmployeeOnboardingStage.class));
    }

    // 이전 스테이지가 완료되지 않았으면 다음 스테이지 진입 실패
    @Test
    void enterStageFailsWhenPreviousStageIsNotCompleted() {
        OnboardingStage previousStage = stage(1L, "핑거 이야기", 1);
        OnboardingStage currentStage = stage(2L, "이메일", 2);
        EmployeeOnboardingStage previousProgress =
                progress(1L, 1L, "IN_PROGRESS");

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(onboardingStageRepository.findById(2L))
                .thenReturn(Optional.of(currentStage));
        when(onboardingStageRepository.findByStageOrder(1))
                .thenReturn(Optional.of(previousStage));
        when(employeeOnboardingStageRepository
                .findByEmployeeIdAndOnboardingStageId(1L, 1L))
                .thenReturn(Optional.of(previousProgress));

        assertError(
                () -> pathService.enterStage(1L, 2L),
                ErrorCode.PREVIOUS_STAGE_NOT_COMPLETED
        );

        verify(employeeOnboardingStageRepository, never())
                .save(any(EmployeeOnboardingStage.class));
    }

    // 이전 스테이지가 완료됐으면 다음 스테이지 진입 가능
    @Test
    void enterStageSucceedsWhenPreviousStageIsCompleted() {
        OnboardingStage previousStage = stage(1L, "핑거 이야기", 1);
        OnboardingStage currentStage = stage(2L, "이메일", 2);
        EmployeeOnboardingStage previousProgress =
                progress(1L, 1L, "COMPLETED");

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(onboardingStageRepository.findById(2L))
                .thenReturn(Optional.of(currentStage));
        when(onboardingStageRepository.findByStageOrder(1))
                .thenReturn(Optional.of(previousStage));
        when(employeeOnboardingStageRepository
                .findByEmployeeIdAndOnboardingStageId(1L, 1L))
                .thenReturn(Optional.of(previousProgress));
        when(employeeOnboardingStageRepository
                .findByEmployeeIdAndOnboardingStageId(1L, 2L))
                .thenReturn(Optional.empty());
        when(employeeOnboardingStageRepository.save(any(EmployeeOnboardingStage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PathStageEnterResponseDto response = pathService.enterStage(1L, 2L);

        assertThat(response.getAllowed()).isTrue();
        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
    }

    // 진입 기록이 없으면 스테이지 완료 처리 실패
    @Test
    void clearStageFailsWhenProgressDoesNotExist() {
        Employee employee = employee(1L, "ONBOARDING");
        OnboardingStage stage = stage(2L, "이메일", 2);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(onboardingStageRepository.findById(2L))
                .thenReturn(Optional.of(stage));
        when(employeeOnboardingStageRepository
                .findByEmployeeIdAndOnboardingStageId(1L, 2L))
                .thenReturn(Optional.empty());

        assertError(
                () -> pathService.clearStage(1L, 2L),
                ErrorCode.STAGE_NOT_ACCESSIBLE
        );

        verify(employeeOnboardingStageRepository, never())
                .save(any(EmployeeOnboardingStage.class));
    }

    // 진입한 스테이지를 COMPLETED 상태로 변경
    @Test
    void clearStageChangesStatusToCompleted() {
        Employee employee = employee(1L, "ONBOARDING");
        OnboardingStage stage = stage(2L, "이메일", 2);
        EmployeeOnboardingStage progress = progress(1L, 2L, "IN_PROGRESS");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(onboardingStageRepository.findById(2L))
                .thenReturn(Optional.of(stage));
        when(employeeOnboardingStageRepository
                .findByEmployeeIdAndOnboardingStageId(1L, 2L))
                .thenReturn(Optional.of(progress));
        when(employeeOnboardingStageRepository.save(progress))
                .thenReturn(progress);
        when(onboardingStageRepository.count()).thenReturn(2L);
        when(employeeOnboardingStageRepository
                .countByEmployeeIdAndStatus(1L, "COMPLETED"))
                .thenReturn(1L);

        PathStageClearResponseDto response = pathService.clearStage(1L, 2L);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(progress.getStatus()).isEqualTo("COMPLETED");
        assertThat(employee.getCurrentPhase()).isEqualTo("ONBOARDING");
    }

    // 모든 스테이지가 완료되면 사원 단계를 COMPLETED로 변경
    @Test
    void clearLastStageChangesEmployeePhaseToCompleted() {
        Employee employee = employee(1L, "ONBOARDING");
        OnboardingStage stage = stage(2L, "이메일", 2);
        EmployeeOnboardingStage progress = progress(1L, 2L, "IN_PROGRESS");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(onboardingStageRepository.findById(2L))
                .thenReturn(Optional.of(stage));
        when(employeeOnboardingStageRepository
                .findByEmployeeIdAndOnboardingStageId(1L, 2L))
                .thenReturn(Optional.of(progress));
        when(employeeOnboardingStageRepository.save(progress))
                .thenReturn(progress);
        when(onboardingStageRepository.count()).thenReturn(2L);
        when(employeeOnboardingStageRepository
                .countByEmployeeIdAndStatus(1L, "COMPLETED"))
                .thenReturn(2L);
        when(employeeRepository.save(employee)).thenReturn(employee);

        pathService.clearStage(1L, 2L);

        assertThat(employee.getCurrentPhase()).isEqualTo("COMPLETED");
        verify(employeeRepository).save(employee);
    }

    // 테스트에서 사용할 스테이지 생성
    private OnboardingStage stage(
            Long stageId,
            String stageName,
            int stageOrder
    ) {
        OnboardingStage stage = new OnboardingStage();
        stage.setOnboardingStageId(stageId);
        stage.setStageName(stageName);
        stage.setStageOrder(stageOrder);
        return stage;
    }

    // 테스트에서 사용할 사원별 스테이지 진행 기록 생성
    private EmployeeOnboardingStage progress(
            Long employeeId,
            Long stageId,
            String status
    ) {
        EmployeeOnboardingStage progress = new EmployeeOnboardingStage();
        progress.setEmployeeId(employeeId);
        progress.setOnboardingStageId(stageId);
        progress.setStatus(status);
        return progress;
    }

    // 테스트에서 사용할 사원 생성
    private Employee employee(Long employeeId, String currentPhase) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setCurrentPhase(currentPhase);
        return employee;
    }

    // 발생한 CustomException의 ErrorCode 확인
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
