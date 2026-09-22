// PoemMyResponseDto
// POST /complete/poem/my 응답 (API-CPL-002 기준, lines는 가변 배열로 확장)

package com.finger.fingerjourneybackend.dto.poem.response;

import lombok.Getter;

import java.util.List;

@Getter
public class PoemMyResponseDto {

    private final List<PoemLineDto> lines;

    public PoemMyResponseDto(List<PoemLineDto> lines) {
        this.lines = lines;
    }
}
