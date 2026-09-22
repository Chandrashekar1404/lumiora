package com.lumiora.enrollment.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;

public interface EnrollmentService {

    Enrollment save(Enrollment enrollment);

    List<Enrollment> findAll();

    Optional<Enrollment> findById(Long id);

    List<Enrollment> findAllByOrganizationId(
            Long organizationId
    );

    Optional<Enrollment> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByStudentAndBatch(
            Long studentId,
            Long batchId
    );

    long countByBatchAndStatus(
            Long batchId,
            EnrollmentStatus status
    );
}