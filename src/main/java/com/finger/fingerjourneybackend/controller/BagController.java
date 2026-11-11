package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.bag.request.BagEnterRequestDto;
import com.finger.fingerjourneybackend.dto.bag.response.BagCompleteResponseDto;
import com.finger.fingerjourneybackend.dto.bag.response.BagEnterResponseDto;
import com.finger.fingerjourneybackend.dto.bag.response.BagListItemResponseDto;
import com.finger.fingerjourneybackend.dto.bag.response.BagStatusItemResponseDto;
import com.finger.fingerjourneybackend.service.AuthService;
import com.finger.fingerjourneybackend.service.BagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bag")
@RequiredArgsConstructor
public class BagController {

    private final BagService bagService;
    private final AuthService authService;

    /**
     * 가방싸기 전체 항목과 로그인 사원의 진행 상태를 조회한다.
     */
    @PostMapping("/list")
    public ApiResponseDto<List<BagListItemResponseDto>> getBagList(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization
    ) {
        Long employeeId =
                authService.getEmployeeId(authorization);

        return new ApiResponseDto<>(
                bagService.getBagList(employeeId)
        );
    }

    /**
     * 관리자의 확인 결과가 반영됐는지 확인하기 위해
     * 로그인 사원의 항목별 최신 상태를 조회한다.
     */
    @PostMapping("/status")
    public ApiResponseDto<List<BagStatusItemResponseDto>> getBagStatus(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization
    ) {
        Long employeeId =
                authService.getEmployeeId(authorization);

        return new ApiResponseDto<>(
                bagService.getBagStatus(employeeId)
        );
    }

    /**
     * 로그인 사원이 특정 가방싸기 항목에 진입한다.
     */
    @PostMapping("/enter")
    public ApiResponseDto<BagEnterResponseDto> enterBagItem(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization,

            @Valid
            @RequestBody
            BagEnterRequestDto request
    ) {
        Long employeeId =
                authService.getEmployeeId(authorization);

        return new ApiResponseDto<>(
                bagService.enterBagItem(
                        employeeId,
                        request.getCheckTypeId()
                )
        );
    }

    /**
     * 모든 가방싸기 항목을 완료하고
     * 사원의 현재 단계를 ONBOARDING으로 변경한다.
     */
    @PostMapping("/complete")
    public ApiResponseDto<BagCompleteResponseDto> completeBag(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization
    ) {
        Long employeeId =
                authService.getEmployeeId(authorization);

        return new ApiResponseDto<>(
                bagService.completeBag(employeeId)
        );
    }
}