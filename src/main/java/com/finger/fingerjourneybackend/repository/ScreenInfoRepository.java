package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.ScreenInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenInfoRepository extends JpaRepository<ScreenInfo, Long> {

    @EntityGraph(attributePaths = "task")
    List<ScreenInfo> findAllByOrderByTaskTaskCodeAscDisplayOrderAsc();
}
