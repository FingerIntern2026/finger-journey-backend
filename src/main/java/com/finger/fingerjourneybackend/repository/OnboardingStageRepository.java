package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.OnboardingStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OnboardingStageRepository
        extends JpaRepository<OnboardingStage, Long> {

    List<OnboardingStage> findAllByOrderByStageOrderAsc(); //전체 스테이지 가져옴

    Optional<OnboardingStage> findByStageOrder( //특정 순서 스테이지 가져옴(POST /path/stage/enter에서 사용)
            Integer stageOrder
    );
}