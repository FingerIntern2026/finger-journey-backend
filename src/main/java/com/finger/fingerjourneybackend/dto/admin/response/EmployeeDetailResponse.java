// EmployeeDetailResponse
// 역할: 상세조회(ADM-002) API의 응답 전용 DTO
// Employee 엔티티를 그대로 반환하지 않고, 필요한 필드만 골라서 반환하기 위해 사용
// (엔티티를 그대로 노출하면 나중에 컬럼이 추가될 때 API 응답에 의도치 않은 정보까지
//  섞여 나갈 수 있어서, 응답 형태를 이 클래스가 고정해줌)
//
// API 명세서 기준 응답 필드: employeeNo, name, organizationName, positionName,
// hireDate, currentPhase
// (organizationName/positionName은 추후 Organization/Position 조인 붙을 때 채워질 필드.
//  지금은 Employee 엔티티에 이름 정보가 없어서 organizationId/positionId로 대체함 - TODO)

package com.finger.fingerjourneybackend.dto.admin.response;

import com.finger.fingerjourneybackend.entity.Employee;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class EmployeeDetailResponse {

    private final Long employeeId;
    private final String employeeNo;
    private final String name;
    private final Long organizationId; // TODO: organizationName으로 교체 필요
    private final Long positionId;     // TODO: positionName으로 교체 필요
    private final LocalDate hireDate;
    private final String currentPhase;

    private EmployeeDetailResponse(Employee employee) {
        this.employeeId = employee.getEmployeeId();
        this.employeeNo = employee.getEmployeeNo();
        this.name = employee.getName();
        this.organizationId = employee.getOrganizationId();
        this.positionId = employee.getPositionId();
        this.hireDate = employee.getHireDate();
        this.currentPhase = employee.getCurrentPhase();
    }

    // Employee 엔티티를 받아서 응답 DTO로 변환하는 정적 팩토리 메서드
    public static EmployeeDetailResponse from(Employee employee) {
        return new EmployeeDetailResponse(employee);
    }
}