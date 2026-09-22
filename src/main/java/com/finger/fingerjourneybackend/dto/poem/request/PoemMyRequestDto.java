// PoemMyRequestDto
// POST /complete/poem/my 요청 바디 — 세션이 없어 employeeId로 "내 3행시"를 특정함

package com.finger.fingerjourneybackend.dto.poem.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PoemMyRequestDto {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long employeeId;
}
