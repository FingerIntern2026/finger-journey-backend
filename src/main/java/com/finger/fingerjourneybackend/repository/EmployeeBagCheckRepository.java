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

    // 사원 삭제 시 가방싸기 진행 기록을 함께 지운다. (API-ADM-005)
    void deleteByEmployeeId(Long employeeId);

    Optional<EmployeeBagCheck> findByEmployeeIdAndCheckTypeId(
            Long employeeId,
            Long checkTypeId
    );

    long countByEmployeeIdAndStatus(
            Long employeeId,
            String status
    );
}