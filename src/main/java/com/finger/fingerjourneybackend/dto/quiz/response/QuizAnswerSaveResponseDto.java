// QuizAnswerSaveResponseDto
// POST /quiz/answers 응답 — 명세서(API-QUZ-002) 기준 저장된 개수만 반환

package com.finger.fingerjourneybackend.dto.quiz.response;

import lombok.Getter;

@Getter
public class QuizAnswerSaveResponseDto {

    private final int savedCount;

    public QuizAnswerSaveResponseDto(int savedCount) {
        this.savedCount = savedCount;
    }
}
