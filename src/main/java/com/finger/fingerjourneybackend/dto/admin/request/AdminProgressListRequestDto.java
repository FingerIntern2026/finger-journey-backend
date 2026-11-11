package com.finger.fingerjourneybackend.dto.admin.request;

import lombok.Getter;

// 진행현황 조회 요청 (API-ADM-008)
// organizationId를 보내면 해당 조직의 사원만, 안 보내면 전체 사원을 조회한다.
@Getter
public class AdminProgressListRequestDto {

    private Long organizationId;
}
