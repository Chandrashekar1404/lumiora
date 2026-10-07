package com.lumiora.certificate.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.lumiora.certificate.dto.CertificateCreateRequest;
import com.lumiora.certificate.dto.CertificateResponse;
import com.lumiora.certificate.dto.CertificateUpdateRequest;

public interface CertificateService {

    CertificateResponse issueCertificate(
            CertificateCreateRequest request,
            Authentication authentication
    );

    List<CertificateResponse> getAllCertificates(
            Authentication authentication
    );

    List<CertificateResponse> getMyCertificates(
            Authentication authentication
    );

    List<CertificateResponse> getCertificatesByStudent(
            Long studentId,
            Authentication authentication
    );

    List<CertificateResponse> getCertificatesByBatch(
            Long batchId,
            Authentication authentication
    );

    CertificateResponse getCertificateById(
            Long certificateId,
            Authentication authentication
    );

    CertificateResponse verifyCertificate(
            String certificateNumber
    );

    CertificateResponse updateCertificate(
            Long certificateId,
            CertificateUpdateRequest request,
            Authentication authentication
    );

    void revokeCertificate(
            Long certificateId,
            Authentication authentication
    );
}