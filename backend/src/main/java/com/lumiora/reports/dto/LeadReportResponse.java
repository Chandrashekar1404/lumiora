package com.lumiora.reports.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeadReportResponse {

    private String scopeType;
    private Long organizationId;

    private String fromDate;
    private String toDate;

    private long totalLeads;

    private long newLeads;
    private long contactedLeads;
    private long interestedLeads;
    private long followUpLeads;
    private long convertedLeads;
    private long lostLeads;

    private BigDecimal conversionPercentage;
}