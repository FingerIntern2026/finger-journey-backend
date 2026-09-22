// QuizAnswerSaveRequestDto
// POST /quiz/answers 요청 바디
// 명세서(API-QUZ-002)는 문항별 선택값을 배열로 일괄 저장하도록 정의함
// 이 프로젝트엔 아직 로그인 세션이 없어서, "내 응답"을 특정하기 위해 employeeId를 같이 받음

package com.finger.fingerjourneybackend.dto.quiz.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class QuizAnswerSaveRequestDto {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long employeeId;

    @NotEmpty(message = "answers는 최소 1개 이상이어야 합니다.")
    @Valid
    private List<AnswerItem> answers;

    @Getter
    public static class AnswerItem {

        @NotNull(message = "quizId는 필수입니다.")
        private Long quizId;

        @NotNull(message = "selectedOption은 필수입니다.")
        private String selectedOption;
    }
}
