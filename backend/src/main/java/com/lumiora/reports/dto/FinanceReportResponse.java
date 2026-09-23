package com.lumiora.reports.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FinanceReportResponse {

    private String scopeType;
    private Long organizationId;

    private String fromDate;
    private String toDate;

    private long totalFeeAccounts;

    private BigDecimal totalFees;
    private BigDecimal totalDiscount;
    private BigDecimal totalPayable;
    private BigDecimal totalPaid;
    private BigDecimal totalBalance;

    private long pendingFeeAccounts;
    private long partiallyPaidFeeAccounts;
    private long paidFeeAccounts;
    private long cancelledFeeAccounts;

    private long totalPayments;
    private long successfulPayments;
    private long cancelledPayments;

    private BigDecimal successfulPaymentAmount;
    private BigDecimal cancelledPaymentAmount;
}