// QuizEvidenceDto
// 리포트 화면의 "근거가 된 퀴즈 답변" 그리드 한 칸
// percentage는 QuizResponse 테이블을 실제로 집계한 값 — AI가 지어내지 않음

package com.finger.fingerjourneybackend.dto.report.response;

import lombok.Getter;

@Getter
public class QuizEvidenceDto {
    private final String question;
    private final String answer;
    private final int percentage; // 이 답변을 고른 전체 응답자 비율 (%)

    public QuizEvidenceDto(String question, String answer, int percentage) {
        this.question = question;
        this.answer = answer;
        this.percentage = percentage;
    }
}
