package com.lumiora.accountant.dto;

import java.math.BigDecimal;

public class FinanceSummaryResponse {

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

    private long totalPayments;
    private BigDecimal successfulPaymentAmount;
    private BigDecimal cancelledPaymentAmount;

    public FinanceSummaryResponse() {
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public long getTotalFeeAccounts() {
        return totalFeeAccounts;
    }

    public void setTotalFeeAccounts(long totalFeeAccounts) {
        this.totalFeeAccounts = totalFeeAccounts;
    }

    public BigDecimal getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(BigDecimal totalFees) {
        this.totalFees = totalFees;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(BigDecimal totalPayable) {
        this.totalPayable = totalPayable;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public long getPendingFeeAccounts() {
        return pendingFeeAccounts;
    }

    public void setPendingFeeAccounts(long pendingFeeAccounts) {
        this.pendingFeeAccounts = pendingFeeAccounts;
    }

    public long getPartiallyPaidFeeAccounts() {
        return partiallyPaidFeeAccounts;
    }

    public void setPartiallyPaidFeeAccounts(long partiallyPaidFeeAccounts) {
        this.partiallyPaidFeeAccounts = partiallyPaidFeeAccounts;
    }

    public long getPaidFeeAccounts() {
        return paidFeeAccounts;
    }

    public void setPaidFeeAccounts(long paidFeeAccounts) {
        this.paidFeeAccounts = paidFeeAccounts;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public BigDecimal getSuccessfulPaymentAmount() {
        return successfulPaymentAmount;
    }

    public void setSuccessfulPaymentAmount(
            BigDecimal successfulPaymentAmount
    ) {
        this.successfulPaymentAmount = successfulPaymentAmount;
    }

    public BigDecimal getCancelledPaymentAmount() {
        return cancelledPaymentAmount;
    }

    public void setCancelledPaymentAmount(
            BigDecimal cancelledPaymentAmount
    ) {
        this.cancelledPaymentAmount = cancelledPaymentAmount;
    }
}