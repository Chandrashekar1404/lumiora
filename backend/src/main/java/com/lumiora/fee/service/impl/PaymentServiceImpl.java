package com.lumiora.fee.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lumiora.audit.entity.AuditAction;
import com.lumiora.audit.service.AuditService;
import com.lumiora.entity.auth.User;
import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.FeeStatus;
import com.lumiora.fee.entity.Payment;
import com.lumiora.fee.entity.PaymentStatus;
import com.lumiora.fee.repository.FeeAccountRepository;
import com.lumiora.fee.repository.PaymentRepository;
import com.lumiora.fee.service.PaymentService;
import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final FeeAccountRepository feeAccountRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

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
    public List<Payment> findAllByOrganizationId(Long organizationId) {
        return paymentRepository.findAllByOrganization_Id(organizationId);
    }

    @Override
    public Optional<Payment> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {
        return paymentRepository.findByIdAndOrganization_Id(id, organizationId);
    }

    @Override
    public List<Payment> findAllByFeeIdAndOrganizationId(
            Long feeId,
            Long organizationId) {
        return paymentRepository.findAllByOrganization_IdAndFeeAccount_Id(
                organizationId,
                feeId
        );
    }

    @Override
    @Transactional
    public Payment recordPayment(FeeAccount suppliedFeeAccount, Payment payment) {
        if (suppliedFeeAccount == null || suppliedFeeAccount.getId() == null) {
            throw new IllegalArgumentException("A valid fee account is required");
        }
        if (payment == null) {
            throw new IllegalArgumentException("Payment details are required");
        }

        // Lock and reload the latest fee account state inside this transaction.
        FeeAccount feeAccount = feeAccountRepository
                .findByIdForUpdate(suppliedFeeAccount.getId())
                .orElseThrow(() -> new IllegalArgumentException("Fee account not found"));

        if (payment.getFeeAccount() != null
                && !feeAccount.getId().equals(payment.getFeeAccount().getId())) {
            throw new IllegalArgumentException(
                    "Payment fee account does not match the requested fee account"
            );
        }

        validateFeeAccountForPayment(feeAccount);
        validateFeeLedger(feeAccount);

        BigDecimal amount = payment.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (amount.compareTo(feeAccount.getBalanceAmount()) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot exceed the pending balance"
            );
        }

        BigDecimal newPaid = feeAccount.getAmountPaid().add(amount);
        BigDecimal newBalance = feeAccount.getBalanceAmount().subtract(amount);

        feeAccount.setAmountPaid(newPaid);
        feeAccount.setBalanceAmount(newBalance);
        feeAccount.setStatus(
                newBalance.compareTo(BigDecimal.ZERO) == 0
                        ? FeeStatus.PAID
                        : FeeStatus.PARTIALLY_PAID
        );

        // Never trust organization/account associations supplied by the caller.
        payment.setFeeAccount(feeAccount);
        payment.setOrganization(feeAccount.getOrganization());
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);
        feeAccountRepository.save(feeAccount);

        User actor = currentUser();
        auditService.record(
                actor.getId(),
                actor.getEmail(),
                AuditAction.PAYMENT,
                "Payment",
                savedPayment.getId(),
                feeAccount.getOrganization().getId(),
                "Payment recorded"
        );

        return savedPayment;
    }

    @Override
    @Transactional
    public Payment cancelPayment(FeeAccount suppliedFeeAccount, Payment suppliedPayment) {
        if (suppliedFeeAccount == null || suppliedFeeAccount.getId() == null) {
            throw new IllegalArgumentException("A valid fee account is required");
        }
        if (suppliedPayment == null || suppliedPayment.getId() == null) {
            throw new IllegalArgumentException("A valid payment is required");
        }

        // Use the same lock order everywhere: fee account, then payment.
        FeeAccount feeAccount = feeAccountRepository
                .findByIdForUpdate(suppliedFeeAccount.getId())
                .orElseThrow(() -> new IllegalArgumentException("Fee account not found"));

        Payment payment = paymentRepository
                .findByIdForUpdate(suppliedPayment.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!feeAccount.getId().equals(payment.getFeeAccount().getId())) {
            throw new IllegalArgumentException(
                    "Payment does not belong to the supplied fee account"
            );
        }
        if (!feeAccount.getOrganization().getId()
                .equals(payment.getOrganization().getId())) {
            throw new IllegalArgumentException(
                    "Payment organization does not match the fee account"
            );
        }
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalArgumentException("Payment is already cancelled");
        }
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Only successful payments can be cancelled");
        }

        BigDecimal amount = payment.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount is invalid");
        }

        validateFeeLedger(feeAccount);
        if (feeAccount.getAmountPaid().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Cannot cancel payment because the fee account paid amount is inconsistent"
            );
        }

        BigDecimal newPaid = feeAccount.getAmountPaid().subtract(amount);
        BigDecimal newBalance = feeAccount.getPayableAmount().subtract(newPaid);

        feeAccount.setAmountPaid(newPaid);
        feeAccount.setBalanceAmount(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            feeAccount.setStatus(FeeStatus.PAID);
        } else if (newPaid.compareTo(BigDecimal.ZERO) == 0) {
            feeAccount.setStatus(FeeStatus.PENDING);
        } else {
            feeAccount.setStatus(FeeStatus.PARTIALLY_PAID);
        }

        payment.setStatus(PaymentStatus.CANCELLED);

        Payment savedPayment = paymentRepository.save(payment);
        feeAccountRepository.save(feeAccount);

        User actor = currentUser();
        auditService.record(
                actor.getId(),
                actor.getEmail(),
                AuditAction.STATUS_CHANGE,
                "Payment",
                savedPayment.getId(),
                feeAccount.getOrganization().getId(),
                "Payment cancelled"
        );

        return savedPayment;
    }

    private void validateFeeAccountForPayment(FeeAccount feeAccount) {
        if (feeAccount.getStatus() == FeeStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot make payment for a cancelled fee account"
            );
        }
        if (!feeAccount.isActive()) {
            throw new IllegalArgumentException("Fee account is inactive");
        }
    }

    private void validateFeeLedger(FeeAccount feeAccount) {
        if (feeAccount.getPayableAmount() == null
                || feeAccount.getAmountPaid() == null
                || feeAccount.getBalanceAmount() == null) {
            throw new IllegalArgumentException(
                    "Fee account contains incomplete balance information"
            );
        }

        BigDecimal expectedBalance = feeAccount.getPayableAmount()
                .subtract(feeAccount.getAmountPaid());

        if (expectedBalance.compareTo(feeAccount.getBalanceAmount()) != 0
                || feeAccount.getAmountPaid().compareTo(BigDecimal.ZERO) < 0
                || feeAccount.getBalanceAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Fee account balance is inconsistent. Please reconcile the fee ledger before continuing."
            );
        }
    }

    private User currentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }
}
