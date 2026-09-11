package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

// 사원 수정 요청 전용 DTO
// employeeId만 필수, 나머지는 선택(null 가능) - 부분 수정용
@Getter
public class EmployeeUpdateRequest {

    @NotNull(message = "employeeId는 필수입니다.")
    private Long employeeId;

    private String name;
    private Long organizationId;
    private Long positionId;
    private LocalDate hireDate;
}