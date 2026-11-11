package com.finger.fingerjourneybackend.dto.bag.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BagListItemResponseDto {

    private Long checkTypeId;
    private String checkName;
    private String iconMediaUrl;
    private Integer displayOrder;
    private String status;
}