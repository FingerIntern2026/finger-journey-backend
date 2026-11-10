package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmployeeId(Long employeeId);
}
