package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long>{
}
