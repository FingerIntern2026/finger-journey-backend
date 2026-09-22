// QuizQuestionResponseDto
// POST /quiz/questions 응답 — 문항 하나
// 명세서(API-QUZ-001) 기준 필드 그대로 반환

package com.finger.fingerjourneybackend.dto.quiz.response;

import com.finger.fingerjourneybackend.entity.Quiz;
import lombok.Getter;

@Getter
public class QuizQuestionResponseDto {

    private final Long quizId;
    private final String question;
    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private final Integer displayOrder;

    private QuizQuestionResponseDto(Long quizId, String question, String option1, String option2,
                                     String option3, String option4, Integer displayOrder) {
        this.quizId = quizId;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.displayOrder = displayOrder;
    }

    public static QuizQuestionResponseDto from(Quiz quiz) {
        return new QuizQuestionResponseDto(
                quiz.getQuizId(),
                quiz.getQuestion(),
                quiz.getOption1(),
                quiz.getOption2(),
                quiz.getOption3(),
                quiz.getOption4(),
                quiz.getDisplayOrder()
        );
    }
}
