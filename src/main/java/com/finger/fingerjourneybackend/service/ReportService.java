// ReportService
// AI 완주 리포트 생성 로직 (팀 설계 문서 "리포트 생성 순서" 그대로 구현)
// 1.사원조회 2.기존리포트 존재시 차단 3.퀴즈응답9개조회 4.3행시조회 5.부족하면 CPL_003
// 6.GENERATING 저장 7.AI서버호출 8.성공시 COMPLETED / 실패시 FAILED 9.응답 반환

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.client.AiReportClient;
import com.finger.fingerjourneybackend.dto.ai.request.AiAcrosticLineDto;
import com.finger.fingerjourneybackend.dto.ai.request.AiQuizResponseDto;
import com.finger.fingerjourneybackend.dto.ai.request.AiReportRequestDto;
import com.finger.fingerjourneybackend.dto.ai.response.AiReportResponseDto;
import com.finger.fingerjourneybackend.dto.report.response.QuizEvidenceDto;
import com.finger.fingerjourneybackend.dto.report.response.ReportResponseDto;
import com.finger.fingerjourneybackend.entity.*;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    // 퀴즈는 9문항 고정 (팀 설계 확정 사항)
    private static final int REQUIRED_QUIZ_COUNT = 9;

    private final EmployeeRepository employeeRepository;
    private final QuizRepository quizRepository;
    private final QuizResponseRepository quizResponseRepository;
    private final ThreeLinePoemRepository threeLinePoemRepository;
    private final ThreeLinePoemLineRepository threeLinePoemLineRepository;
    private final AiReportRepository aiReportRepository;
    private final AiReportClient aiReportClient;

    public ReportResponseDto generateReport(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 사원별 완료된 리포트는 최대 1개 — 이미 COMPLETED면 재생성 차단
        // (GENERATING/FAILED로 남아있는 이전 시도는 막지 않고 같은 행을 재사용해서 재시도 허용)
        AiReport report = aiReportRepository.findByEmployeeId(employeeId).orElse(null);
        if (report != null && ReportStatus.COMPLETED.name().equals(report.getStatus())) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        List<QuizResponse> quizResponses = quizResponseRepository.findByEmployeeId(employeeId);
        ThreeLinePoem poem = threeLinePoemRepository.findByEmployeeId(employeeId).orElse(null);

        if (quizResponses.size() < REQUIRED_QUIZ_COUNT || poem == null) {
            throw new CustomException(ErrorCode.REPORT_PREREQUISITE_NOT_MET);
        }

        List<ThreeLinePoemLine> poemLines = threeLinePoemLineRepository.findByPoemIdOrderByLineOrderAsc(poem.getPoemId());

        if (report == null) {
            report = new AiReport();
            report.setEmployeeId(employeeId);
        }
        report.setStatus(ReportStatus.GENERATING.name());
        aiReportRepository.save(report);

        AiReportRequestDto aiRequest = buildAiRequest(employee, quizResponses, poemLines);

        try {
            AiReportResponseDto aiResponse = aiReportClient.generateReport(aiRequest);
            report.setStatus(ReportStatus.COMPLETED.name());
            report.setHeadline(aiResponse.getHeadline());
            report.setReportContent(aiResponse.getReportContent());
            report.setKeywords(String.join(",", aiResponse.getKeywords()));
            report.setQuote(aiResponse.getQuote());
            report.setQuoteDescription(aiResponse.getQuoteDescription());
            report.setGeneratedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("AI 리포트 생성 실패 (employeeId={})", employeeId, e);
            report.setStatus(ReportStatus.FAILED.name());
        }

        report = aiReportRepository.save(report);

        List<QuizEvidenceDto> quizEvidence = buildQuizEvidence(quizResponses);
        return ReportResponseDto.from(report, employee.getName(), quizEvidence);
    }

    // 기존에 생성된 리포트 조회 (API-CPL-004 대응)
    public ReportResponseDto getReportDetail(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        AiReport report = aiReportRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        List<QuizResponse> quizResponses = quizResponseRepository.findByEmployeeId(employeeId);
        List<QuizEvidenceDto> quizEvidence = buildQuizEvidence(quizResponses);

        return ReportResponseDto.from(report, employee.getName(), quizEvidence);
    }

    private AiReportRequestDto buildAiRequest(Employee employee, List<QuizResponse> quizResponses, List<ThreeLinePoemLine> poemLines) {
        List<AiQuizResponseDto> quizDtos = quizResponses.stream()
                .map(qr -> new AiQuizResponseDto(getQuestionText(qr.getQuizId()), qr.getSelectedOption()))
                .toList();

        List<AiAcrosticLineDto> lineDtos = poemLines.stream()
                .map(line -> new AiAcrosticLineDto(line.getLetter(), line.getText()))
                .toList();

        return new AiReportRequestDto(employee.getName(), quizDtos, lineDtos);
    }

    // "근거가 된 퀴즈 답변" 그리드 — QuizResponse 테이블을 실제로 집계해서 비율을 계산함
    // (AI는 이 숫자를 모름, 순전히 백엔드 계산)
    private List<QuizEvidenceDto> buildQuizEvidence(List<QuizResponse> quizResponses) {
        return quizResponses.stream()
                .map(qr -> {
                    long total = quizResponseRepository.countByQuizId(qr.getQuizId());
                    long sameAnswer = quizResponseRepository.countByQuizIdAndSelectedOption(qr.getQuizId(), qr.getSelectedOption());
                    int percentage = total == 0 ? 0 : (int) Math.round(sameAnswer * 100.0 / total);
                    return new QuizEvidenceDto(getQuestionText(qr.getQuizId()), qr.getSelectedOption(), percentage);
                })
                .toList();
    }

    private String getQuestionText(Long quizId) {
        return quizRepository.findById(quizId).map(Quiz::getQuestion).orElse("");
    }
}
