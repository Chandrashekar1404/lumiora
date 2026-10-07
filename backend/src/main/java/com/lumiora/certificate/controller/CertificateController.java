package com.lumiora.certificate.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.certificate.dto.CertificateCreateRequest;
import com.lumiora.certificate.dto.CertificateResponse;
import com.lumiora.certificate.dto.CertificateUpdateRequest;
import com.lumiora.certificate.service.CertificateService;
import com.lumiora.dto.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    // =========================================================
    // ISSUE CERTIFICATE
    // =========================================================

    @PostMapping
    public ResponseEntity<
            ApiResponse<CertificateResponse>>
            issueCertificate(
                    @Valid @RequestBody
                    CertificateCreateRequest request,
                    Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Certificate issued successfully",
                                certificateService
                                        .issueCertificate(
                                                request,
                                                authentication
                                        )
                        )
                );
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<CertificateResponse>>>
            getAllCertificates(
                    Authentication authentication) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Certificates fetched successfully",
                        certificateService
                                .getAllCertificates(
                                        authentication
                                )
                )
        );
    }

    // =========================================================
    // MY CERTIFICATES
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<
            ApiResponse<List<CertificateResponse>>>
            getMyCertificates(
                    Authentication authentication) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "My certificates fetched successfully",
                        certificateService
                                .getMyCertificates(
                                        authentication
                                )
                )
        );
    }

    // =========================================================
    // BY STUDENT
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<
            ApiResponse<List<CertificateResponse>>>
            getCertificatesByStudent(
                    @PathVariable Long studentId,
                    Authentication authentication) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student certificates fetched successfully",
                        certificateService
                                .getCertificatesByStudent(
                                        studentId,
                                        authentication
                                )
                )
        );
    }

    // =========================================================
    // BY BATCH
    // =========================================================

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<
            ApiResponse<List<CertificateResponse>>>
            getCertificatesByBatch(
                    @PathVariable Long batchId,
                    Authentication authentication) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch certificates fetched successfully",
                        certificateService
                                .getCertificatesByBatch(
                                        batchId,
                                        authentication
                                )
                )
        );
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<
            ApiResponse<CertificateResponse>>
            getCertificateById(
                    @PathVariable Long id,
                    Authentication authentication) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Certificate fetched successfully",
                        certificateService
                                .getCertificateById(
                                        id,
                                        authentication
                                )
                )
        );
    }

    // =========================================================
    // PUBLIC VERIFICATION
    // =========================================================

    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<
            ApiResponse<CertificateResponse>>
            verifyCertificate(
                    @PathVariable String certificateNumber) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Certificate is valid",
                        certificateService
                                .verifyCertificate(
                                        certificateNumber
                                )
                )
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<
            ApiResponse<CertificateResponse>>
            updateCertificate(
                    @PathVariable Long id,
                    @Valid @RequestBody
                    CertificateUpdateRequest request,
                    Authentication authentication) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Certificate updated successfully",
                        certificateService
                                .updateCertificate(
                                        id,
                                        request,
                                        authentication
                                )
                )
        );
    }

    // =========================================================
    // REVOKE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            revokeCertificate(
                    @PathVariable Long id,
                    Authentication authentication) {

        certificateService.revokeCertificate(
                id,
                authentication
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Certificate revoked successfully",
                        null
                )
        );
    }
}