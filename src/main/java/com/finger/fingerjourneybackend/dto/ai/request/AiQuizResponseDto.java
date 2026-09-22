// AiQuizResponseDto
// Spring이 AI 서버(finger-journey-ai)에 퀴즈 응답을 넘길 때 문항 하나를 담는 그릇

package com.finger.fingerjourneybackend.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiQuizResponseDto {
    private String question;
    private String selectedOption;
}
