// AiReportRequestDto
// Spring → finger-journey-ai(FastAPI) POST /report/generate 요청 바디

package com.finger.fingerjourneybackend.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiReportRequestDto {
    private String employeeName;
    private List<AiQuizResponseDto> quizResponses;
    private List<AiAcrosticLineDto> acrosticLines;
}
