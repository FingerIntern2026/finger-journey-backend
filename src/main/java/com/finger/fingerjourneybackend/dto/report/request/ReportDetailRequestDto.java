// ReportDetailRequestDto
// POST /api/reports/detail 요청 바디 — 기존에 생성된 리포트를 조회할 때 사용

package com.finger.fingerjourneybackend.dto.report.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReportDetailRequestDto {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long employeeId;
}
