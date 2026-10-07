package com.lumiora.certificate.dto;

import java.time.LocalDate;

import com.lumiora.certificate.entity.CertificateStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {

    private Long id;

    private String certificateNumber;

    private LocalDate issueDate;

    private String achievement;

    private String remarks;

    private CertificateStatus status;

    private boolean active;

    private Long enrollmentId;

    private Long studentId;

    private String studentName;

    private String studentEmail;

    private Long courseId;

    private String courseName;

    private Long batchId;

    private String batchName;

    private String batchCode;

    private Long organizationId;

    private String organizationName;

    private Long issuedById;

    private String issuedByName;
}