package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.OnboardingStageMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnboardingStageMediaRepository
        extends JpaRepository<OnboardingStageMedia, Long> {

    List<OnboardingStageMedia>
    findByOnboardingStageIdOrderByDisplayOrderAsc(
            Long onboardingStageId
    );
}