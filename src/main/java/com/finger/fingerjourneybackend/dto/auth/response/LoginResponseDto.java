package com.finger.fingerjourneybackend.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

// 로그인 성공 시 반환하는 직원 정보와 인증 토큰
@Getter
@Builder
public class LoginResponseDto {
    private Long employeeId;
    private String name;
    private String organizationName;
    private String positionName;
    private String currentPhase;
    private String adminRole;
    private String redirectScreen;
    private String accessToken;
    private String refreshToken;
}
