// PoemController
// 완주 3행시 저장 / 조회 (API-CPL-001~002)

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.poem.request.PoemMyRequestDto;
import com.finger.fingerjourneybackend.dto.poem.request.PoemSaveRequestDto;
import com.finger.fingerjourneybackend.dto.poem.response.PoemMyResponseDto;
import com.finger.fingerjourneybackend.dto.poem.response.PoemSaveResponseDto;
import com.finger.fingerjourneybackend.service.PoemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/complete/poem")
@RequiredArgsConstructor
public class PoemController {

    private final PoemService poemService;

    // 저장: POST /complete/poem
    // 요청 body: { "employeeId": 4, "name": "최준비", "lines": ["...", "...", "..."] }
    @PostMapping
    public ApiResponseDto<PoemSaveResponseDto> savePoem(@Valid @RequestBody PoemSaveRequestDto request) {
        return new ApiResponseDto<>(poemService.savePoem(request));
    }

    // 조회: POST /complete/poem/my
    // 요청 body: { "employeeId": 4 }
    @PostMapping("/my")
    public ApiResponseDto<PoemMyResponseDto> getMyPoem(@Valid @RequestBody PoemMyRequestDto request) {
        return new ApiResponseDto<>(poemService.getMyPoem(request.getEmployeeId()));
    }
}
