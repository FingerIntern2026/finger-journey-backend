package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.ScreenInfoResponseDto;
import com.finger.fingerjourneybackend.entity.BackAction;
import com.finger.fingerjourneybackend.entity.ScreenInfo;
import com.finger.fingerjourneybackend.entity.Task;
import com.finger.fingerjourneybackend.repository.ScreenInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreenInfoServiceTest {

    @Mock
    private ScreenInfoRepository screenInfoRepository;

    @InjectMocks
    private ScreenInfoService screenInfoService;

    @Test
    void getScreenListMapsTaskAndNavigationFields() {
        Task task = new Task();
        task.setTaskCode("DEMO");
        task.setExitScreenCode("DEMO_HOM_P01");

        ScreenInfo screen = new ScreenInfo();
        screen.setTask(task);
        screen.setScreenCode("DEMO_MOV_P01");
        screen.setScreenName("화면 이동 가이드");
        screen.setRoutePath("/demo/move");
        screen.setBackAction(BackAction.TARGET);
        screen.setBackScreenCode("DEMO_HOM_P01");
        screen.setDisplayOrder(2);

        when(screenInfoRepository.findAllByOrderByTaskTaskCodeAscDisplayOrderAsc())
                .thenReturn(List.of(screen));

        List<ScreenInfoResponseDto> result = screenInfoService.getScreenList();

        assertThat(result).hasSize(1);
        ScreenInfoResponseDto response = result.get(0);
        assertThat(response.getTaskCode()).isEqualTo("DEMO");
        assertThat(response.getScreenCode()).isEqualTo("DEMO_MOV_P01");
        assertThat(response.getRoutePath()).isEqualTo("/demo/move");
        assertThat(response.getBackAction()).isEqualTo(BackAction.TARGET);
        assertThat(response.getBackScreenCode()).isEqualTo("DEMO_HOM_P01");
        assertThat(response.getExitScreenCode()).isEqualTo("DEMO_HOM_P01");
        assertThat(response.getDisplayOrder()).isEqualTo(2);
    }
}
