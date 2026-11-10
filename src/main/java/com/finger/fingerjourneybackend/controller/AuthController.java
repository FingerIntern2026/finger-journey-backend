package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.auth.request.LoginRequestDto;
import com.finger.fingerjourneybackend.dto.auth.response.LoginResponseDto;
import com.finger.fingerjourneybackend.dto.auth.response.SessionResponseDto;
import com.finger.fingerjourneybackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponseDto<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return new ApiResponseDto<>(authService.authenticate(request));
    }

    @PostMapping("/session")
    public ApiResponseDto<SessionResponseDto> session(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return new ApiResponseDto<>(authService.getSession(authorization));
    }
}
