package com.finger.fingerjourneybackend.dto.admin.response;

import com.finger.fingerjourneybackend.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 사원 등록 성공 응답 전용 DTO
@Getter
@AllArgsConstructor
public class EmployeeCreateResponse {

    private Long employeeId;

    // Employee 엔티티를 이 응답 DTO로 변환하는 정적 팩토리 메서드
    public static EmployeeCreateResponse from(Employee employee) {
        return new EmployeeCreateResponse(employee.getEmployeeId());
    }
}