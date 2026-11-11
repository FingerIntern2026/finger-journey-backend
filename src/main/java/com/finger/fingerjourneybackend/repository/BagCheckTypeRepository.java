package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.BagCheckType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BagCheckTypeRepository
        extends JpaRepository<BagCheckType, Long> {

    List<BagCheckType> findAllByOrderByDisplayOrderAsc();
}