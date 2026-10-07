package com.lumiora.certificate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.certificate.entity.Certificate;

public interface CertificateRepository
        extends JpaRepository<Certificate, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "enrollment",
            "student",
            "course",
            "batch",
            "batch.course",
            "batch.trainer",
            "organization",
            "issuedBy"
    })
    List<Certificate> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "enrollment",
            "student",
            "course",
            "batch",
            "batch.course",
            "batch.trainer",
            "organization",
            "issuedBy"
    })
    Optional<Certificate> findById(Long id);

    @EntityGraph(attributePaths = {
            "enrollment",
            "student",
            "course",
            "batch",
            "batch.course",
            "batch.trainer",
            "organization",
            "issuedBy"
    })
    Optional<Certificate> findByCertificateNumber(
            String certificateNumber
    );

    @EntityGraph(attributePaths = {
            "enrollment",
            "student",
            "course",
            "batch",
            "batch.course",
            "batch.trainer",
            "organization",
            "issuedBy"
    })
    List<Certificate> findAllByOrganization_Id(
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "enrollment",
            "student",
            "course",
            "batch",
            "batch.course",
            "batch.trainer",
            "organization",
            "issuedBy"
    })
    List<Certificate> findAllByStudent_IdAndOrganization_Id(
            Long studentId,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "enrollment",
            "student",
            "course",
            "batch",
            "batch.course",
            "batch.trainer",
            "organization",
            "issuedBy"
    })
    List<Certificate> findAllByBatch_IdAndOrganization_Id(
            Long batchId,
            Long organizationId
    );

    boolean existsByEnrollment_Id(Long enrollmentId);
}