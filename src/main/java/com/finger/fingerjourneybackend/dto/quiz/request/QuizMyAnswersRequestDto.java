// QuizMyAnswersRequestDto
// POST /quiz/answers/my 요청 바디 — 세션이 없어 employeeId로 "내 응답"을 특정함

package com.finger.fingerjourneybackend.dto.quiz.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class QuizMyAnswersRequestDto {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long employeeId;
}
