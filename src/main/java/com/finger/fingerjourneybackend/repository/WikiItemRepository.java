package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.WikiItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiItemRepository extends JpaRepository<WikiItem, Long> {
}
