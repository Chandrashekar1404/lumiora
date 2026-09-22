package com.lumiora.enrollment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    List<Enrollment> findAll();

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    Optional<Enrollment> findById(Long id);

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    List<Enrollment> findAllByOrganization_Id(
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    Optional<Enrollment> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    boolean existsByStudent_IdAndBatch_Id(
            Long studentId,
            Long batchId
    );

    long countByBatch_IdAndStatus(
            Long batchId,
            EnrollmentStatus status
    );
}