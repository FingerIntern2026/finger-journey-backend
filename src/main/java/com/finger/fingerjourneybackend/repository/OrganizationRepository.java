package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
