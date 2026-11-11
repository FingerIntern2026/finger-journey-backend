package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.EmployeeBagCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//기록 있음 → EmployeeBagCheck 하나
//기록 없음 → 빈 값

public interface EmployeeBagCheckRepository
        extends JpaRepository<EmployeeBagCheck, Long> {

    List<EmployeeBagCheck> findByEmployeeId(Long employeeId);

    Optional<EmployeeBagCheck> findByEmployeeIdAndCheckTypeId(
            Long employeeId,
            Long checkTypeId
    );

    long countByEmployeeIdAndStatus(
            Long employeeId,
            String status
    );
}