package com.finger.fingerjourneybackend.dto.admin.response;

import com.finger.fingerjourneybackend.entity.Organization;
import lombok.Builder;
import lombok.Getter;

// 조직 목록 조회 응답 (API-ADM-006)
@Getter
@Builder
public class OrganizationListResponseDto {
    private Long organizationId;
    private String organizationName;
    private String organizationType;
    private Long parentId;

    public static OrganizationListResponseDto from(Organization organization) {
        return OrganizationListResponseDto.builder()
                .organizationId(organization.getOrganizationId())
                .organizationName(organization.getOrganizationName())
                .organizationType(organization.getOrganizationType())
                .parentId(organization.getParentId())
                .build();
    }
}
