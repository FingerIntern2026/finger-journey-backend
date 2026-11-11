package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmployeeId(Long employeeId);

    // 사원 삭제 시 관리자 권한도 함께 지운다. (API-ADM-005)
    void deleteByEmployeeId(Long employeeId);
}
