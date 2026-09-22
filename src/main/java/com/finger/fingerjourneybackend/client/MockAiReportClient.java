// MockAiReportClient
// 리포트 생성 전체 흐름(사원/퀴즈/3행시 검증 → 상태 저장 → 결과 저장)을 실제 AI 서버 없이
// 먼저 검증하는 데 썼던 가짜 구현체. RealAiReportClient로 교체 완료되어 지금은 @Component가
// 아니라서 스프링 빈으로 등록되지 않음 (AI 서버 장애 시 임시로 되돌리는 용도로 남겨둠 —
// 되돌리려면 이 클래스에 @Component 붙이고 RealAiReportClient에서 빼면 됨)

package com.finger.fingerjourneybackend.client;

import com.finger.fingerjourneybackend.dto.ai.request.AiReportRequestDto;
import com.finger.fingerjourneybackend.dto.ai.response.AiReportResponseDto;

import java.util.List;

public class MockAiReportClient implements AiReportClient {

    @Override
    public AiReportResponseDto generateReport(AiReportRequestDto request) {
        String content = request.getEmployeeName() + "님은 새로운 환경에서도 적극적으로 소통하며 "
                + "성장하는 유형입니다. (Mock 응답 — 실제 AI 서버 연동 전 임시 결과)";
        return new AiReportResponseDto(
                "🌱 적극적으로 소통하는 성장형",
                content,
                List.of("협업", "성장", "적극성", "도전"),
                "천천히, 그러나 꾸준히.",
                "Mock 응답 — 실제 AI 서버 연동 전 임시 결과"
        );
    }
}
