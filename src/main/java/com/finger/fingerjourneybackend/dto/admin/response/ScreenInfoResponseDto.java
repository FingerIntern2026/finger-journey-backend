package com.finger.fingerjourneybackend.dto.admin.response;

import com.finger.fingerjourneybackend.entity.BackAction;
import com.finger.fingerjourneybackend.entity.ScreenInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScreenInfoResponseDto {

    private String taskCode;
    private String screenCode;
    private String screenName;
    private String routePath;
    private boolean loginRequired;
    private BackAction backAction;
    private String backScreenCode;
    private String exitScreenCode;
    private Integer displayOrder;

    public static ScreenInfoResponseDto from(ScreenInfo screenInfo) {
        return ScreenInfoResponseDto.builder()
                .taskCode(screenInfo.getTask().getTaskCode())
                .screenCode(screenInfo.getScreenCode())
                .screenName(screenInfo.getScreenName())
                .routePath(screenInfo.getRoutePath())
                .loginRequired(screenInfo.isLoginRequired())
                .backAction(screenInfo.getBackAction())
                .backScreenCode(screenInfo.getBackScreenCode())
                .exitScreenCode(screenInfo.getTask().getExitScreenCode())
                .displayOrder(screenInfo.getDisplayOrder())
                .build();
    }
}
