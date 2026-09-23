package com.lumiora.reports.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.reports.dto.AttendanceReportResponse;
import com.lumiora.reports.dto.EnrollmentReportResponse;
import com.lumiora.reports.dto.FinanceReportResponse;
import com.lumiora.reports.dto.LeadReportResponse;
import com.lumiora.reports.dto.OverviewReportResponse;
import com.lumiora.reports.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    @GetMapping("/overview")
    public ResponseEntity<OverviewReportResponse> getOverviewReport(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {

        return ResponseEntity.ok(
                reportService.getOverviewReport(
                        authentication,
                        fromDate,
                        toDate
                )
        );
    }


    @GetMapping("/finance")
    public ResponseEntity<FinanceReportResponse> getFinanceReport(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {

        return ResponseEntity.ok(
                reportService.getFinanceReport(
                        authentication,
                        fromDate,
                        toDate
                )
        );
    }


    @GetMapping("/attendance")
    public ResponseEntity<AttendanceReportResponse> getAttendanceReport(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {

        return ResponseEntity.ok(
                reportService.getAttendanceReport(
                        authentication,
                        fromDate,
                        toDate
                )
        );
    }


    @GetMapping("/enrollments")
    public ResponseEntity<EnrollmentReportResponse> getEnrollmentReport(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {

        return ResponseEntity.ok(
                reportService.getEnrollmentReport(
                        authentication,
                        fromDate,
                        toDate
                )
        );
    }


    @GetMapping("/leads")
    public ResponseEntity<LeadReportResponse> getLeadReport(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {

        return ResponseEntity.ok(
                reportService.getLeadReport(
                        authentication,
                        fromDate,
                        toDate
                )
        );
    }
}