package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.client.AiReportClient;
import com.finger.fingerjourneybackend.entity.AiReport;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.ReportStatus;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock QuizRepository quizRepository;
    @Mock QuizResponseRepository quizResponseRepository;
    @Mock ThreeLinePoemRepository threeLinePoemRepository;
    @Mock ThreeLinePoemLineRepository threeLinePoemLineRepository;
    @Mock AiReportRepository aiReportRepository;
    @Mock AiReportClient aiReportClient;
    @InjectMocks ReportService reportService;

    @Test
    void generateReportFailsWhenQuizOrPoemIsIncomplete() {
        Employee employee = new Employee();
        employee.setEmployeeId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(aiReportRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());
        when(quizResponseRepository.findByEmployeeId(1L)).thenReturn(List.of());
        when(threeLinePoemRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());

        assertError(() -> reportService.generateReport(1L), ErrorCode.REPORT_PREREQUISITE_NOT_MET);
        verify(aiReportRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(aiReportClient, never()).generateReport(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void generateReportFailsWhenCompletedReportAlreadyExists() {
        Employee employee = new Employee();
        employee.setEmployeeId(1L);
        AiReport report = new AiReport();
        report.setStatus(ReportStatus.COMPLETED.name());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(aiReportRepository.findByEmployeeId(1L)).thenReturn(Optional.of(report));

        assertError(() -> reportService.generateReport(1L), ErrorCode.REPORT_ALREADY_EXISTS);
        verify(aiReportClient, never()).generateReport(org.mockito.ArgumentMatchers.any());
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
