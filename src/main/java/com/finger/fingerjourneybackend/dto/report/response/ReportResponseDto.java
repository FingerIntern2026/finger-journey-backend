// ReportResponseDto
// Spring → 프론트 POST /api/reports/generate 응답 바디
// AiReport 엔티티의 keywords는 DB에 콤마 문자열로 저장돼 있어서, 여기서 배열로 변환해 내려줌

package com.finger.fingerjourneybackend.dto.report.response;

import com.finger.fingerjourneybackend.entity.AiReport;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
public class ReportResponseDto {

    private final Long reportId;
    private final Long employeeId;
    private final String employeeName;
    private final String status;
    private final String headline;
    private final String reportContent;
    private final List<String> keywords;
    private final String quote;
    private final String quoteDescription;
    private final LocalDateTime generatedAt;

    private ReportResponseDto(Long reportId, Long employeeId, String employeeName, String status, String headline,
                               String reportContent, List<String> keywords, String quote, String quoteDescription,
                               LocalDateTime generatedAt) {
        this.reportId = reportId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.status = status;
        this.headline = headline;
        this.reportContent = reportContent;
        this.keywords = keywords;
        this.quote = quote;
        this.quoteDescription = quoteDescription;
        this.generatedAt = generatedAt;
    }

    public static ReportResponseDto from(AiReport report, String employeeName) {
        List<String> keywordList = (report.getKeywords() == null || report.getKeywords().isBlank())
                ? List.of()
                : Arrays.asList(report.getKeywords().split(","));

        return new ReportResponseDto(
                report.getReportId(),
                report.getEmployeeId(),
                employeeName,
                report.getStatus(),
                report.getHeadline(),
                report.getReportContent(),
                keywordList,
                report.getQuote(),
                report.getQuoteDescription(),
                report.getGeneratedAt()
        );
    }
}
