package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
