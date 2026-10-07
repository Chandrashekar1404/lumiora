package com.lumiora.certificate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CertificateCreateRequest {

    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @Size(
            max = 500,
            message = "Achievement cannot exceed 500 characters"
    )
    private String achievement;

    @Size(
            max = 1000,
            message = "Remarks cannot exceed 1000 characters"
    )
    private String remarks;
}