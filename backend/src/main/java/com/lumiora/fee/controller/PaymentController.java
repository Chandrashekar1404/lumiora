package com.lumiora.fee.controller;

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

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.entity.auth.User;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.fee.dto.PaymentCreateRequest;
import com.lumiora.fee.dto.PaymentResponse;
import com.lumiora.fee.dto.PaymentUpdateRequest;

import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.Payment;
import com.lumiora.fee.entity.PaymentStatus;

import com.lumiora.fee.service.FeeService;
import com.lumiora.fee.service.PaymentService;

import com.lumiora.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private final FeeService feeService;

    private final UserService userService;


    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @PostMapping("/fees/{feeId}/payments")
    public ResponseEntity<?> createPayment(
            @PathVariable Long feeId,
            @Valid @RequestBody PaymentCreateRequest request,
            Authentication authentication) {

        boolean isSuperAdmin =
                isSuperAdmin(authentication);

        FeeAccount feeAccount;

        if (isSuperAdmin) {

            feeAccount =
                    feeService.findById(feeId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Fee account not found with id: "
                                                    + feeId
                                    ));

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            feeAccount =
                    feeService
                            .findByIdAndOrganizationId(
                                    feeId,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Fee account not found with id: "
                                                    + feeId
                                    ));
        }

        Payment payment =
                new Payment();

        payment.setFeeAccount(
                feeAccount
        );

        payment.setOrganization(
                feeAccount.getOrganization()
        );

        payment.setAmount(
                request.getAmount()
        );

        payment.setPaymentDate(
                request.getPaymentDate()
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setReferenceNumber(
                request.getReferenceNumber()
        );

        payment.setInstallmentNumber(
                request.getInstallmentNumber()
        );

        payment.setNotes(
                request.getNotes()
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        try {

            Payment savedPayment =
                    paymentService.recordPayment(
                            feeAccount,
                            payment
                    );

            Payment refreshedPayment =
                    paymentService.findById(
                                    savedPayment.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Payment not found after creation"
                                    ));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    "Payment recorded successfully",
                                    toResponse(
                                            refreshedPayment
                                    )
                            )
                    );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    exception.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>>
            getAllPayments(
                    Authentication authentication) {

        List<Payment> payments;

        if (isSuperAdmin(authentication)) {

            payments =
                    paymentService.findAll();

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
                );
            }

            payments =
                    paymentService
                            .findAllByOrganizationId(
                                    currentUser
                                            .getOrganization()
                                            .getId()
                            );
        }

        List<PaymentResponse> responses =
                payments.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payments fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET PAYMENTS BY FEE
    // =========================================================

    @GetMapping("/fees/{feeId}/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>>
            getPaymentsByFee(
                    @PathVariable Long feeId,
                    Authentication authentication) {

        boolean isSuperAdmin =
                isSuperAdmin(authentication);

        if (isSuperAdmin) {

            feeService.findById(feeId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Fee account not found with id: "
                                            + feeId
                            ));

            FeeAccount fee =
                    feeService.findById(feeId)
                            .orElseThrow();

            List<Payment> payments =
                    paymentService
                            .findAllByOrganizationId(
                                    fee.getOrganization()
                                            .getId()
                            )
                            .stream()
                            .filter(payment ->
                                    payment.getFeeAccount()
                                            .getId()
                                            .equals(feeId)
                            )
                            .toList();

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Payments fetched successfully",
                            payments.stream()
                                    .map(this::toResponse)
                                    .toList()
                    )
            );
        }

        User currentUser =
                getCurrentUser(authentication);

        if (currentUser.getOrganization() == null) {

            throw new IllegalStateException(
                    "User is not assigned to an organization"
            );
        }

        Long organizationId =
                currentUser.getOrganization().getId();

        feeService
                .findByIdAndOrganizationId(
                        feeId,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Fee account not found with id: "
                                        + feeId
                        ));

        List<Payment> payments =
                paymentService
                        .findAllByFeeIdAndOrganizationId(
                                feeId,
                                organizationId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payments fetched successfully",
                        payments.stream()
                                .map(this::toResponse)
                                .toList()
                )
        );
    }


    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>>
            getPaymentById(
                    @PathVariable Long id,
                    Authentication authentication) {

        Payment payment;

        if (isSuperAdmin(authentication)) {

            payment =
                    paymentService.findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Payment not found with id: "
                                                    + id
                                    ));

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
                );
            }

            payment =
                    paymentService
                            .findByIdAndOrganizationId(
                                    id,
                                    currentUser
                                            .getOrganization()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Payment not found with id: "
                                                    + id
                                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment fetched successfully",
                        toResponse(payment)
                )
        );
    }


    // =========================================================
    // UPDATE PAYMENT METADATA
    // =========================================================

    @PutMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>>
            updatePayment(
                    @PathVariable Long id,
                    @RequestBody PaymentUpdateRequest request,
                    Authentication authentication) {

        Payment payment =
                getAccessiblePayment(
                        id,
                        authentication
                );

        if (payment.getStatus()
                == PaymentStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot update a cancelled payment"
            );
        }

        if (request.getPaymentMethod() != null) {

            payment.setPaymentMethod(
                    request.getPaymentMethod()
            );
        }

        if (request.getReferenceNumber() != null) {

            payment.setReferenceNumber(
                    request.getReferenceNumber()
            );
        }

        if (request.getNotes() != null) {

            payment.setNotes(
                    request.getNotes()
            );
        }

        Payment updatedPayment =
                paymentService.save(payment);

        Payment refreshedPayment =
                paymentService
                        .findById(updatedPayment.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Payment not found after update"
                                ));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment updated successfully",
                        toResponse(refreshedPayment)
                )
        );
    }


    // =========================================================
    // CANCEL PAYMENT
    // =========================================================

    @DeleteMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<Void>>
            cancelPayment(
                    @PathVariable Long id,
                    Authentication authentication) {

        Payment payment =
                getAccessiblePayment(
                        id,
                        authentication
                );

        FeeAccount feeAccount =
                payment.getFeeAccount();

        try {

            paymentService.cancelPayment(
                    feeAccount,
                    payment
            );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    exception.getMessage()
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment cancelled successfully",
                        null
                )
        );
    }


    // =========================================================
    // ACCESSIBLE PAYMENT
    // =========================================================

    private Payment getAccessiblePayment(
            Long id,
            Authentication authentication) {

        if (isSuperAdmin(authentication)) {

            return paymentService
                    .findById(id)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Payment not found with id: "
                                            + id
                            ));
        }

        User currentUser =
                getCurrentUser(authentication);

        if (currentUser.getOrganization() == null) {

            throw new IllegalStateException(
                    "User is not assigned to an organization"
            );
        }

        return paymentService
                .findByIdAndOrganizationId(
                        id,
                        currentUser
                                .getOrganization()
                                .getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found with id: "
                                        + id
                        ));
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser(
            Authentication authentication) {

        return userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Logged-in user not found"
                        )
                );
    }


    // =========================================================
    // SUPER ADMIN
    // =========================================================

    private boolean isSuperAdmin(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_SUPER_ADMIN")
                );
    }


    // =========================================================
    // RESPONSE
    // =========================================================

    private PaymentResponse toResponse(
            Payment payment) {

        FeeAccount fee =
                payment.getFeeAccount();

        User student =
                fee.getEnrollment()
                        .getStudent();

        String studentName =
                (
                        student.getFirstName()
                                + " "
                                + (
                                student.getLastName() != null
                                        ? student.getLastName()
                                        : ""
                        )
                ).trim();

        return new PaymentResponse(
                payment.getId(),
                fee.getId(),
                fee.getEnrollment().getId(),
                payment.getOrganization().getId(),
                student.getId(),
                studentName,
                student.getEmail(),
                fee.getEnrollment()
                        .getBatch()
                        .getId(),
                fee.getEnrollment()
                        .getBatch()
                        .getName(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getPaymentMethod(),
                payment.getReferenceNumber(),
                payment.getInstallmentNumber(),
                payment.getNotes(),
                payment.getStatus()
        );
    }
}