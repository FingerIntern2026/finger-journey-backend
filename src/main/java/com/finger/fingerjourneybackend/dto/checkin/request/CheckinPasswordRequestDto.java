package com.finger.fingerjourneybackend.dto.checkin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

// 체크인 비밀번호 설정(관리자)과 확인(입사자)이 같이 쓰는 요청
@Getter
public class CheckinPasswordRequestDto {

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(max = 50, message = "비밀번호는 50자 이하여야 합니다.")
    private String password;
}
