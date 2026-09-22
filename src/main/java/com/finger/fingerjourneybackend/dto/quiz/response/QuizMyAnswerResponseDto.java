// QuizMyAnswerResponseDto
// POST /quiz/answers/my 응답 — 명세서(API-QUZ-003) 기준, 문항+내가 고른 답 하나

package com.finger.fingerjourneybackend.dto.quiz.response;

import lombok.Getter;

@Getter
public class QuizMyAnswerResponseDto {

    private final Long quizId;
    private final String question;
    private final String selectedOption;

    public QuizMyAnswerResponseDto(Long quizId, String question, String selectedOption) {
        this.quizId = quizId;
        this.question = question;
        this.selectedOption = selectedOption;
    }
}
