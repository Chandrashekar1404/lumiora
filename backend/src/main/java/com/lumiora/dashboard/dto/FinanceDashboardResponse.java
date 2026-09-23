package com.lumiora.dashboard.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FinanceDashboardResponse {

    private String scopeType;

    private Long organizationId;

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

    private long overdueFeeAccounts;

    private long totalPayments;

    private long successfulPaymentCount;
    private BigDecimal successfulPaymentAmount;

    private long cancelledPaymentCount;
    private BigDecimal cancelledPaymentAmount;
}