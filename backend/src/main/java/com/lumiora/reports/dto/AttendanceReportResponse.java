package com.lumiora.reports.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceReportResponse {

    private String scopeType;
    private Long organizationId;

    private String fromDate;
    private String toDate;

    private long totalRecords;

    private long presentCount;
    private long absentCount;
    private long lateCount;
    private long halfDayCount;

    private BigDecimal attendancePercentage;
}