package com.finger.fingerjourneybackend.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

// 로그인 요청: 사번과 생년월일(YYYYMMDD)
@Getter
public class LoginRequestDto {

    @NotBlank(message = "사번은 필수입니다.")
    @Pattern(regexp = "[0-9]{8}", message = "사번은 숫자 8자리여야 합니다.")
    private String empNo;

    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(regexp = "[0-9]{8}", message = "생년월일은 YYYYMMDD 형식의 숫자 8자리여야 합니다.")
    private String birthDate;
}
