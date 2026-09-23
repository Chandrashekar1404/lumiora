package com.lumiora.reports.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EnrollmentReportResponse {

    private String scopeType;
    private Long organizationId;

    private String fromDate;
    private String toDate;

    private long totalEnrollments;

    private long enrolledCount;
    private long completedCount;
    private long droppedCount;
    private long cancelledCount;
}