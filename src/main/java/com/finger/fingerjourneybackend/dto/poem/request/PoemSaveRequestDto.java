// PoemSaveRequestDto
// POST /complete/poem 요청 바디
// ERD 변경(9/22 팀 설계)으로 line1~3 고정 대신 lines 배열(이름 글자수만큼 2~4개)을 받음

package com.finger.fingerjourneybackend.dto.poem.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class PoemSaveRequestDto {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long employeeId;

    @NotBlank(message = "name은 필수입니다.")
    private String name;

    @NotEmpty(message = "lines는 최소 1개 이상이어야 합니다.")
    private List<String> lines;
}
