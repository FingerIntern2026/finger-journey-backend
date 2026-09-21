package com.finger.fingerjourneybackend.dto.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finger.fingerjourneybackend.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 사원 수정 성공 응답 전용 DTO
@Getter
@AllArgsConstructor
public class EmployeeUpdateResponse {

    private Long employeeId;

    // 명세서 형식(yyyy-MM-dd HH:mm:ss)에 맞게 날짜 포맷을 강제 지정
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static EmployeeUpdateResponse from(Employee employee) {
        return new EmployeeUpdateResponse(employee.getEmployeeId(), employee.getUpdatedAt());
    }
}