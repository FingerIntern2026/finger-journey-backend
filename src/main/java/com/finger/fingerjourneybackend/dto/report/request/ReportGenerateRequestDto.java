// ReportGenerateRequestDto
// 프론트 → Spring POST /api/reports/generate 요청 바디
// 프론트는 employeeId만 넘기고, 퀴즈·3행시는 Spring이 DB에서 직접 조회함

package com.finger.fingerjourneybackend.dto.report.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReportGenerateRequestDto {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long employeeId;
}
