package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.OrganizationListResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.PositionListResponseDto;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.entity.Position;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCodeServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private AdminCodeService adminCodeService;

    @Test
    void organizationListKeepsHierarchyInformation() {
        Organization division = new Organization();
        division.setOrganizationId(1L);
        division.setOrganizationName("개발본부");
        division.setOrganizationType("DIVISION");
        Organization team = new Organization();
        team.setOrganizationId(2L);
        team.setOrganizationName("개발1팀");
        team.setOrganizationType("TEAM");
        team.setParentId(1L);
        when(organizationRepository.findAll(Sort.by("organizationId"))).thenReturn(List.of(division, team));

        List<OrganizationListResponseDto> response = adminCodeService.getOrganizationList();

        assertThat(response).extracting(OrganizationListResponseDto::getOrganizationName)
                .containsExactly("개발본부", "개발1팀");
        assertThat(response).extracting(OrganizationListResponseDto::getOrganizationType)
                .containsExactly("DIVISION", "TEAM");
        assertThat(response).extracting(OrganizationListResponseDto::getParentId).containsExactly(null, 1L);
    }

    @Test
    void positionListIsOrderedByPositionOrder() {
        Position staff = new Position();
        staff.setPositionId(1L);
        staff.setPositionName("사원");
        staff.setPositionOrder(1);
        when(positionRepository.findAll(Sort.by("positionOrder"))).thenReturn(List.of(staff));

        List<PositionListResponseDto> response = adminCodeService.getPositionList();

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.getPositionId()).isEqualTo(1L);
            assertThat(item.getPositionName()).isEqualTo("사원");
            assertThat(item.getPositionOrder()).isEqualTo(1);
        });
    }
}
