package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.EmployeeOnboardingStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeOnboardingStageRepository extends JpaRepository<EmployeeOnboardingStage, Long> {
    long countByEmployeeIdAndStatus(Long employeeId, String status);

    // 사원 삭제 시 스테이지 진행 기록을 함께 지운다. (API-ADM-005)
    void deleteByEmployeeId(Long employeeId);
}
