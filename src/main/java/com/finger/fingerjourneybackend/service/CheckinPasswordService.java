// CheckinPasswordService
// 역할: 입사자가 체크인 화면에 들어갈 때 입력하는 "우리만의 암호"를 관리한다.
// - 암호는 전체 공통 하나이고, HR(관리자)이 설정한다.
// - HR이 말로 알려주는 공유 문구라서 해시하지 않고 그대로 저장한다. (HR이 현재 값을 다시 볼 수 있도록)
// - 앞뒤 공백은 저장·비교할 때 모두 제거한다.

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.entity.AppSetting;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class CheckinPasswordService {

    public static final String SETTING_KEY = "CHECKIN_PASSWORD";

    private final AppSettingRepository appSettingRepository;

    /**
     * 체크인 비밀번호 설정 (관리자, API-ADM-019)
     * - 이미 값이 있으면 덮어쓰고, 없으면 새로 만든다.
     */
    @Transactional
    public void update(String password) {
        AppSetting setting = appSettingRepository.findById(SETTING_KEY).orElseGet(() -> {
            AppSetting created = new AppSetting();
            created.setSettingKey(SETTING_KEY);
            return created;
        });

        setting.setSettingValue(password.trim());
        appSettingRepository.save(setting);
    }

    /**
     * 체크인 비밀번호 확인 (입사자, API-CHK-001)
     * - 맞으면 그대로 끝나고, 틀리면 CHK_001로 응답한다.
     * - 설정된 값이 아직 없으면 누구도 통과하지 못한다. (기본 시드가 있어 보통은 없는 경우)
     */
    @Transactional(readOnly = true)
    public void verify(String password) {
        boolean matches = appSettingRepository.findById(SETTING_KEY)
                .map(AppSetting::getSettingValue)
                .filter(saved -> sameText(saved, password.trim()))
                .isPresent();

        if (!matches) {
            throw new CustomException(ErrorCode.CHECKIN_PASSWORD_MISMATCH);
        }
    }

    // 걸리는 시간으로 값을 짐작하지 못하도록 길이와 관계없이 끝까지 비교한다.
    private boolean sameText(String saved, String input) {
        return MessageDigest.isEqual(
                saved.getBytes(StandardCharsets.UTF_8),
                input.getBytes(StandardCharsets.UTF_8));
    }
}
