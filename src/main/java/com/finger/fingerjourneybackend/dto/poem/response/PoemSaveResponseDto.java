// PoemSaveResponseDto
// POST /complete/poem 응답 (API-CPL-001 기준)

package com.finger.fingerjourneybackend.dto.poem.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PoemSaveResponseDto {

    private final Long poemId;
    private final LocalDateTime savedAt;

    public PoemSaveResponseDto(Long poemId, LocalDateTime savedAt) {
        this.poemId = poemId;
        this.savedAt = savedAt;
    }
}
