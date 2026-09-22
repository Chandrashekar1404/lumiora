package com.lumiora.fee.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.Payment;

public interface PaymentService {

    Payment save(Payment payment);

    List<Payment> findAll();

    Optional<Payment> findById(Long id);

    List<Payment> findAllByOrganizationId(
            Long organizationId
    );

    Optional<Payment> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<Payment> findAllByFeeIdAndOrganizationId(
            Long feeId,
            Long organizationId
    );

    Payment recordPayment(
            FeeAccount feeAccount,
            Payment payment
    );

    Payment cancelPayment(
            FeeAccount feeAccount,
            Payment payment
    );
}