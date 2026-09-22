// RealAiReportClient
// finger-journey-ai(FastAPI)의 POST /report/generate를 실제로 호출하는 구현체
// MockAiReportClient를 대체함 (ReportService는 AiReportClient 인터페이스만 의존하므로
// 이 클래스가 유일한 @Component가 되는 순간 자동으로 이 구현체가 주입됨)

package com.finger.fingerjourneybackend.client;

import com.finger.fingerjourneybackend.dto.ai.request.AiReportRequestDto;
import com.finger.fingerjourneybackend.dto.ai.response.AiReportResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RealAiReportClient implements AiReportClient {

    private final RestClient aiRestClient;

    @Override
    public AiReportResponseDto generateReport(AiReportRequestDto request) {
        return aiRestClient.post()
                .uri("/report/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiReportResponseDto.class);
    }
}
