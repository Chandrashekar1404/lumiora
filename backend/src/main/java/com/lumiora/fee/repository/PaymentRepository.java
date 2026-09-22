package com.lumiora.fee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.fee.entity.Payment;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "feeAccount",
            "feeAccount.enrollment",
            "feeAccount.enrollment.student",
            "feeAccount.enrollment.batch",
            "feeAccount.enrollment.batch.course"
    })
    List<Payment> findAll();

    @EntityGraph(attributePaths = {
            "organization",
            "feeAccount",
            "feeAccount.enrollment",
            "feeAccount.enrollment.student",
            "feeAccount.enrollment.batch",
            "feeAccount.enrollment.batch.course"
    })
    Optional<Payment> findById(Long id);

    @EntityGraph(attributePaths = {
            "organization",
            "feeAccount",
            "feeAccount.enrollment",
            "feeAccount.enrollment.student",
            "feeAccount.enrollment.batch",
            "feeAccount.enrollment.batch.course"
    })
    List<Payment> findAllByOrganization_Id(
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "feeAccount",
            "feeAccount.enrollment",
            "feeAccount.enrollment.student",
            "feeAccount.enrollment.batch",
            "feeAccount.enrollment.batch.course"
    })
    Optional<Payment> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "feeAccount",
            "feeAccount.enrollment",
            "feeAccount.enrollment.student",
            "feeAccount.enrollment.batch",
            "feeAccount.enrollment.batch.course"
    })
    List<Payment> findAllByOrganization_IdAndFeeAccount_Id(
            Long organizationId,
            Long feeAccountId
    );
}