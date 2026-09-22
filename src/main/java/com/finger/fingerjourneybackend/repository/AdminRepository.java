package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
