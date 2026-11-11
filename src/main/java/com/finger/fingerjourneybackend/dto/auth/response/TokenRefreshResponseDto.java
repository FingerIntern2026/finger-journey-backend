package com.finger.fingerjourneybackend.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
}
