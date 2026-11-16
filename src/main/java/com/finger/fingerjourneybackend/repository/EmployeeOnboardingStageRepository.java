package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.EmployeeOnboardingStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeOnboardingStageRepository
        extends JpaRepository<EmployeeOnboardingStage, Long> {
    
    List<EmployeeOnboardingStage> findByEmployeeId( //어떤 사원의 전체 진행 상태(모든 스테이지 상태)조회
            Long employeeId
    );

    Optional<EmployeeOnboardingStage>
    findByEmployeeIdAndOnboardingStageId( //어떤 사원 + 어떤 스테이지
            Long employeeId,
            Long onboardingStageId
    );

    long countByEmployeeIdAndStatus(
            Long employeeId,
            String status
    );

    // 사원 삭제 시 스테이지 진행 기록을 함께 지운다.
    void deleteByEmployeeId(Long employeeId);
}