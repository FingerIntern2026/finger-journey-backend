// AiReportResponseDto
// finger-journey-ai(FastAPI)가 돌려주는 리포트 결과

package com.finger.fingerjourneybackend.dto.ai.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiReportResponseDto {
    private String headline;
    private String reportContent;
    private List<String> keywords;
    private String quote;
    private String quoteDescription;
}
