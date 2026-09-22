package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.WikiCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiCategoryRepository extends JpaRepository<WikiCategory, Long> {
}
