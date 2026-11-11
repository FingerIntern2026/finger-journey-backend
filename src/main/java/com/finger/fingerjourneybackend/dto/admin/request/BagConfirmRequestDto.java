package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

// 가방싸기 확인 처리 요청 (API-ADM-010)
@Getter
public class BagConfirmRequestDto {

    @NotNull(message = "employeeId는 필수입니다.")
    private Long employeeId;

    @NotNull(message = "checkTypeId는 필수입니다.")
    private Long checkTypeId;
}
