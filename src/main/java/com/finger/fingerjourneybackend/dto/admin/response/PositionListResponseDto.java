package com.finger.fingerjourneybackend.dto.admin.response;

import com.finger.fingerjourneybackend.entity.Position;
import lombok.Builder;
import lombok.Getter;

// 직급 목록 조회 응답 (API-ADM-007)
@Getter
@Builder
public class PositionListResponseDto {
    private Long positionId;
    private String positionName;
    private Integer positionOrder;

    public static PositionListResponseDto from(Position position) {
        return PositionListResponseDto.builder()
                .positionId(position.getPositionId())
                .positionName(position.getPositionName())
                .positionOrder(position.getPositionOrder())
                .build();
    }
}
