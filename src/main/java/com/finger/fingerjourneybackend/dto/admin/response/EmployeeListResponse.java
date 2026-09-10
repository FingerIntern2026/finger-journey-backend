package com.finger.fingerjourneybackend.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeListResponse {
    private Long employeeId;
    private String employeeNo;
    private String name;
    private String organizationName;
    private String positionName;
    private String currentPhase;
    private Integer progressRate;
}
