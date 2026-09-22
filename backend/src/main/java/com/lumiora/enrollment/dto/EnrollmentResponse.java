package com.lumiora.enrollment.dto;

import java.time.LocalDate;

import com.lumiora.enrollment.entity.EnrollmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long id;

    private LocalDate enrollmentDate;

    private EnrollmentStatus status;

    private String notes;

    private Long organizationId;

    private Long batchId;

    private String batchName;

    private String batchCode;

    private Long studentId;

    private String studentName;

    private String studentEmail;
}