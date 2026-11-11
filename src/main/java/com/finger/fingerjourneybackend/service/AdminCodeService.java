// AdminCodeService
// 역할: 사원 등록·수정 폼의 드롭다운을 채우는 조직·직급 목록 조회 (API-ADM-006, 007)

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.OrganizationListResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.PositionListResponseDto;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCodeService {

    private final OrganizationRepository organizationRepository;
    private final PositionRepository positionRepository;

    // 조직 ID 순으로 전체 조직(GROUP/DIVISION/TEAM)을 반환한다.
    public List<OrganizationListResponseDto> getOrganizationList() {
        return organizationRepository.findAll(Sort.by("organizationId")).stream()
                .map(OrganizationListResponseDto::from)
                .toList();
    }

    // position_order 순으로 전체 직급을 반환한다.
    public List<PositionListResponseDto> getPositionList() {
        return positionRepository.findAll(Sort.by("positionOrder")).stream()
                .map(PositionListResponseDto::from)
                .toList();
    }
}
