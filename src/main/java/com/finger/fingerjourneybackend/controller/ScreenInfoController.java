package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.ScreenInfoResponseDto;
import com.finger.fingerjourneybackend.service.ScreenInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/screen")
@RequiredArgsConstructor
public class ScreenInfoController {

    private final ScreenInfoService screenInfoService;

    @PostMapping("/list")
    public ApiResponseDto<List<ScreenInfoResponseDto>> getScreenList() {
        return new ApiResponseDto<>(screenInfoService.getScreenList());
    }
}
