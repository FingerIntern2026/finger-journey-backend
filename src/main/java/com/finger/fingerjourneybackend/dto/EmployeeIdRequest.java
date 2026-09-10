// EmployeeIdRequest
// 역할: employeeId 하나만 필요한 요청(상세조회, 삭제)에 공통으로 쓰는 요청 DTO
// API 명세서 기준: { "employeeId": 1 } 형태의 요청 body를 받음
//
// @NotNull: employeeId가 없거나 null이면 컨트롤러 진입 전에 자동으로 400 에러 발생

package com.finger.fingerjourneybackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeIdRequest {

    @NotNull(message = "employeeId는 필수입니다.")
    private Long employeeId;
}