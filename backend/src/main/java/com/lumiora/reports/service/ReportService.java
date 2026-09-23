package com.lumiora.reports.service;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;

import com.lumiora.reports.dto.AttendanceReportResponse;
import com.lumiora.reports.dto.EnrollmentReportResponse;
import com.lumiora.reports.dto.FinanceReportResponse;
import com.lumiora.reports.dto.LeadReportResponse;
import com.lumiora.reports.dto.OverviewReportResponse;

public interface ReportService {

    OverviewReportResponse getOverviewReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    );

    FinanceReportResponse getFinanceReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    );

    AttendanceReportResponse getAttendanceReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    );

    EnrollmentReportResponse getEnrollmentReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    );

    LeadReportResponse getLeadReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    );
}