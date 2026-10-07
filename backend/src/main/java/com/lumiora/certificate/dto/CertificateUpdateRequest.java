package com.lumiora.certificate.dto;

import com.lumiora.certificate.entity.CertificateStatus;

import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CertificateUpdateRequest {

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

    private CertificateStatus status;

    private Boolean active;
}