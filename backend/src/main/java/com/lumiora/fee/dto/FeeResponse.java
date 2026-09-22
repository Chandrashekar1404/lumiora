package com.lumiora.fee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lumiora.fee.entity.FeeStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeResponse {

    private Long id;

    private Long enrollmentId;

    private Long organizationId;

    private Long studentId;

    private String studentName;

    private String studentEmail;

    private Long batchId;

    private String batchName;

    private String batchCode;

    private String courseName;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payableAmount;

    private BigDecimal amountPaid;

    private BigDecimal balanceAmount;

    private LocalDate dueDate;

    private FeeStatus status;

    private boolean active;
}