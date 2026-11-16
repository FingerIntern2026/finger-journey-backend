package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.config.OpenApiConfig;
import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.path.request.PathStageRequestDto;
import com.finger.fingerjourneybackend.dto.path.response.PathMapItemResponseDto;
import com.finger.fingerjourneybackend.dto.path.response.PathStageClearResponseDto;
import com.finger.fingerjourneybackend.dto.path.response.PathStageEnterResponseDto;
import com.finger.fingerjourneybackend.service.AuthService;
import com.finger.fingerjourneybackend.service.PathService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/path")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class PathController {

    private final PathService pathService;
    private final AuthService authService;

    // 전체 스테이지와 로그인 사원의 진행 상태 조회
    @PostMapping("/map")
    public ApiResponseDto<List<PathMapItemResponseDto>> getPathMap(
            @RequestHeader(
                    value = "Authorization",
                    required = false //헤더가 없으면 Controller 진입 후 authService에서 인증 오류 발생
            )
            String authorization
    ) {
        Long employeeId =
                authService.getEmployeeId(authorization);

        return new ApiResponseDto<>(
                pathService.getPathMap(employeeId)
        );
    }

    // 이전 단계 완료 여부 확인 후 스테이지 진입
    @PostMapping("/stage/enter")
    public ApiResponseDto<PathStageEnterResponseDto> enterStage(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization,

            @Valid
            @RequestBody
            PathStageRequestDto request
    ) {
        Long employeeId =
                authService.getEmployeeId(authorization);

        return new ApiResponseDto<>(
                pathService.enterStage(
                        employeeId,
                        request.getStageId()
                )
        );
    }

    // 진입한 스테이지를 완료 상태로 변경
    @PostMapping("/stage/clear")
    public ApiResponseDto<PathStageClearResponseDto> clearStage(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization,

            @Valid
            @RequestBody
            PathStageRequestDto request
    ) {
        Long employeeId =
                authService.getEmployeeId(authorization);

        return new ApiResponseDto<>(
                pathService.clearStage(
                        employeeId,
                        request.getStageId()
                )
        );
    }
}
