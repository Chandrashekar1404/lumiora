package com.lumiora.fee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.fee.entity.FeeAccount;

public interface FeeAccountRepository
                extends JpaRepository<FeeAccount, Long> {

        @EntityGraph(attributePaths = {
                        "organization",
                        "enrollment",
                        "enrollment.student",
                        "enrollment.batch",
                        "enrollment.batch.course"
        })
        List<FeeAccount> findAll();

        @EntityGraph(attributePaths = {
                        "organization",
                        "enrollment",
                        "enrollment.student",
                        "enrollment.batch",
                        "enrollment.batch.course"
        })
        Optional<FeeAccount> findById(Long id);

        @EntityGraph(attributePaths = {
                        "organization",
                        "enrollment",
                        "enrollment.student",
                        "enrollment.batch",
                        "enrollment.batch.course"
        })
        List<FeeAccount> findAllByOrganization_Id(
                        Long organizationId);

        @EntityGraph(attributePaths = {
                        "organization",
                        "enrollment",
                        "enrollment.student",
                        "enrollment.batch",
                        "enrollment.batch.course"
        })
        Optional<FeeAccount> findByIdAndOrganization_Id(
                        Long id,
                        Long organizationId);

        boolean existsByEnrollment_IdAndOrganization_Id(
                        Long enrollmentId,
                        Long organizationId);

        @EntityGraph(attributePaths = {
                        "organization",
                        "enrollment",
                        "enrollment.student",
                        "enrollment.batch",
                        "enrollment.batch.course"
        })
        Optional<FeeAccount> findByEnrollment_IdAndOrganization_Id(
                        Long enrollmentId,
                        Long organizationId);

        @EntityGraph(attributePaths = {
                        "organization",
                        "enrollment",
                        "enrollment.student",
                        "enrollment.batch",
                        "enrollment.batch.course"
        })
        List<FeeAccount> findAllByOrganization_IdAndEnrollment_Student_Id(
                        Long organizationId,
                        Long studentId);
}