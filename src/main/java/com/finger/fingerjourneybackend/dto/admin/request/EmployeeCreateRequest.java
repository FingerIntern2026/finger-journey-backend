package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

// 사원 등록 요청 전용 DTO
// employeeId, currentPhase는 아예 필드로 안 두어서, 클라이언트가 보내도 무시됨 (덮어쓰기 원천 차단)
@Getter
public class EmployeeCreateRequest {

    @NotBlank(message = "사번은 필수입니다.")
    private String employeeNo;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "조직 ID는 필수입니다.")
    private Long organizationId;

    @NotNull(message = "직급 ID는 필수입니다.")
    private Long positionId;

    @NotNull(message = "입사일은 필수입니다.")
    private LocalDate hireDate;
}