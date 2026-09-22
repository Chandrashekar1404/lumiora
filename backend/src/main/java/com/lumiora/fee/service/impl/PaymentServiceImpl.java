package com.lumiora.fee.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.FeeStatus;
import com.lumiora.fee.entity.Payment;
import com.lumiora.fee.entity.PaymentStatus;
import com.lumiora.fee.repository.FeeAccountRepository;
import com.lumiora.fee.repository.PaymentRepository;
import com.lumiora.fee.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl
        implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final FeeAccountRepository feeAccountRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public List<Payment> findAllByOrganizationId(
            Long organizationId) {

        return paymentRepository
                .findAllByOrganization_Id(
                        organizationId
                );
    }

    @Override
    public Optional<Payment> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return paymentRepository
                .findByIdAndOrganization_Id(
                        id,
                        organizationId
                );
    }

    @Override
    public List<Payment> findAllByFeeIdAndOrganizationId(
            Long feeId,
            Long organizationId) {

        return paymentRepository
                .findAllByOrganization_IdAndFeeAccount_Id(
                        organizationId,
                        feeId
                );
    }

    @Override
    @Transactional
    public Payment recordPayment(
            FeeAccount feeAccount,
            Payment payment) {

        if (feeAccount.getStatus()
                == FeeStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot make payment for a cancelled fee account"
            );
        }

        if (!feeAccount.isActive()) {

            throw new IllegalArgumentException(
                    "Fee account is inactive"
            );
        }

        if (payment.getAmount() == null ||
                payment.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (payment.getAmount()
                .compareTo(
                        feeAccount.getBalanceAmount()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Payment amount cannot exceed the pending balance"
            );
        }

        BigDecimal newPaid =
                feeAccount.getAmountPaid()
                        .add(payment.getAmount());

        BigDecimal newBalance =
                feeAccount.getPayableAmount()
                        .subtract(newPaid);

        feeAccount.setAmountPaid(newPaid);
        feeAccount.setBalanceAmount(newBalance);

        if (newBalance.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            feeAccount.setStatus(
                    FeeStatus.PAID
            );

        } else {

            feeAccount.setStatus(
                    FeeStatus.PARTIALLY_PAID
            );
        }

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        feeAccountRepository.save(feeAccount);

        return savedPayment;
    }

    @Override
    @Transactional
    public Payment cancelPayment(
            FeeAccount feeAccount,
            Payment payment) {

        if (payment.getStatus()
                == PaymentStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Payment is already cancelled"
            );
        }

        BigDecimal newPaid =
                feeAccount.getAmountPaid()
                        .subtract(payment.getAmount());

        if (newPaid.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            newPaid = BigDecimal.ZERO;
        }

        BigDecimal newBalance =
                feeAccount.getPayableAmount()
                        .subtract(newPaid);

        feeAccount.setAmountPaid(newPaid);
        feeAccount.setBalanceAmount(newBalance);

        if (newPaid.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            feeAccount.setStatus(
                    FeeStatus.PENDING
            );

        } else {

            feeAccount.setStatus(
                    FeeStatus.PARTIALLY_PAID
            );
        }

        payment.setStatus(
                PaymentStatus.CANCELLED
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        feeAccountRepository.save(feeAccount);

        return savedPayment;
    }
}