package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.entity.AppSetting;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.AppSettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckinPasswordServiceTest {

    @Mock
    private AppSettingRepository appSettingRepository;

    @InjectMocks
    private CheckinPasswordService checkinPasswordService;

    @Test
    void updateCreatesTheSettingWhenItDoesNotExist() {
        when(appSettingRepository.findById(CheckinPasswordService.SETTING_KEY)).thenReturn(Optional.empty());

        checkinPasswordService.update("  우리만의 암호  ");

        ArgumentCaptor<AppSetting> saved = ArgumentCaptor.forClass(AppSetting.class);
        verify(appSettingRepository).save(saved.capture());
        assertThat(saved.getValue().getSettingKey()).isEqualTo("CHECKIN_PASSWORD");
        assertThat(saved.getValue().getSettingValue()).isEqualTo("우리만의 암호");
    }

    @Test
    void updateOverwritesTheExistingValue() {
        AppSetting existing = setting("finger");
        when(appSettingRepository.findById(CheckinPasswordService.SETTING_KEY)).thenReturn(Optional.of(existing));

        checkinPasswordService.update("new-password");

        verify(appSettingRepository).save(existing);
        assertThat(existing.getSettingValue()).isEqualTo("new-password");
    }

    @Test
    void verifyPassesWhenThePasswordMatches() {
        when(appSettingRepository.findById(CheckinPasswordService.SETTING_KEY))
                .thenReturn(Optional.of(setting("finger")));

        assertThatCode(() -> checkinPasswordService.verify("finger")).doesNotThrowAnyException();
    }

    @Test
    void verifyIgnoresSurroundingSpacesOfTheInput() {
        when(appSettingRepository.findById(CheckinPasswordService.SETTING_KEY))
                .thenReturn(Optional.of(setting("finger")));

        assertThatCode(() -> checkinPasswordService.verify(" finger ")).doesNotThrowAnyException();
    }

    @Test
    void verifyFailsWhenThePasswordDoesNotMatch() {
        when(appSettingRepository.findById(CheckinPasswordService.SETTING_KEY))
                .thenReturn(Optional.of(setting("finger")));

        assertMismatch("Finger");
    }

    @Test
    void verifyFailsWhenNoPasswordIsSet() {
        when(appSettingRepository.findById(CheckinPasswordService.SETTING_KEY)).thenReturn(Optional.empty());

        assertMismatch("finger");
    }

    private void assertMismatch(String password) {
        assertThatThrownBy(() -> checkinPasswordService.verify(password))
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(ErrorCode.CHECKIN_PASSWORD_MISMATCH);
    }

    private AppSetting setting(String value) {
        AppSetting setting = new AppSetting();
        setting.setSettingKey(CheckinPasswordService.SETTING_KEY);
        setting.setSettingValue(value);
        return setting;
    }
}
