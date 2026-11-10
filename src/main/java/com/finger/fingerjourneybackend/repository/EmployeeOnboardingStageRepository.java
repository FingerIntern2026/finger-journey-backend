package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.EmployeeOnboardingStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeOnboardingStageRepository extends JpaRepository<EmployeeOnboardingStage, Long> {
    long countByEmployeeIdAndStatus(Long employeeId, String status);
}
