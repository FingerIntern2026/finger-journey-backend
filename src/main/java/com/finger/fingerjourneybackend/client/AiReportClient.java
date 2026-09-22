// AiReportClient
// Spring이 finger-journey-ai(FastAPI)를 호출하는 창구
// 지금은 MockAiReportClient만 존재 (실제 AI 서버 연동은 다음 단계에서 이 인터페이스의
// 다른 구현체로 교체 — ReportService는 이 인터페이스만 알면 되므로 교체해도 영향 없음)

package com.finger.fingerjourneybackend.client;

import com.finger.fingerjourneybackend.dto.ai.request.AiReportRequestDto;
import com.finger.fingerjourneybackend.dto.ai.response.AiReportResponseDto;

public interface AiReportClient {
    AiReportResponseDto generateReport(AiReportRequestDto request);
}
