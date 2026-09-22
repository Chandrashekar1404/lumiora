package com.lumiora.fee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lumiora.fee.entity.PaymentMethod;
import com.lumiora.fee.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;

    private Long feeAccountId;

    private Long enrollmentId;

    private Long organizationId;

    private Long studentId;

    private String studentName;

    private String studentEmail;

    private Long batchId;

    private String batchName;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private Integer installmentNumber;

    private String notes;

    private PaymentStatus status;
}