// ReportController
// AI 완주 리포트 생성 API
// 프론트는 employeeId만 넘기고, 퀴즈·3행시 조회/AI 서버 호출/저장은 전부 ReportService가 처리

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.report.request.ReportDetailRequestDto;
import com.finger.fingerjourneybackend.dto.report.request.ReportGenerateRequestDto;
import com.finger.fingerjourneybackend.dto.report.response.ReportResponseDto;
import com.finger.fingerjourneybackend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 리포트 생성: POST /api/reports/generate
    // 요청 body: { "employeeId": 1 }
    @PostMapping("/generate")
    public ApiResponseDto<ReportResponseDto> generate(@Valid @RequestBody ReportGenerateRequestDto request) {
        return new ApiResponseDto<>(reportService.generateReport(request.getEmployeeId()));
    }

    // 기존 리포트 조회: POST /api/reports/detail
    // 요청 body: { "employeeId": 1 }
    @PostMapping("/detail")
    public ApiResponseDto<ReportResponseDto> detail(@Valid @RequestBody ReportDetailRequestDto request) {
        return new ApiResponseDto<>(reportService.getReportDetail(request.getEmployeeId()));
    }
}
