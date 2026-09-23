package com.lumiora.accountant.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.accountant.dto.AccountantResponse;
import com.lumiora.accountant.dto.AccountantUpdateRequest;
import com.lumiora.accountant.dto.FinanceSummaryResponse;
import com.lumiora.accountant.service.AccountantService;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.fee.dto.FeeResponse;
import com.lumiora.fee.dto.PaymentResponse;
import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.FeeStatus;
import com.lumiora.fee.entity.Payment;
import com.lumiora.fee.entity.PaymentStatus;

import com.lumiora.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accountants")
@RequiredArgsConstructor
public class AccountantController {

    private final AccountantService accountantService;

    private final UserService userService;


    // =========================================================
    // GET ALL ACCOUNTANTS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountantResponse>>>
            getAllAccountants(
                    Authentication authentication
            ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();

        List<User> accountants;


        if ("SUPER_ADMIN".equals(role)) {

            accountants =
                    accountantService.findAllAccountants();

        } else if ("ADMIN".equals(role)) {

            validateOrganization(currentUser);

            accountants =
                    accountantService
                            .findAllAccountantsByOrganization(
                                    currentUser
                                            .getOrganization()
                                            .getId()
                            );

        } else if ("ACCOUNTANT".equals(role)) {

            accountants =
                    List.of(currentUser);

        } else {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ApiResponse.failure(
                                    "You are not authorized to view accountants"
                            )
                    );
        }


        List<AccountantResponse> responses =
                accountants.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Accountants fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET MY PROFILE
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountantResponse>>
            getMyProfile(
                    Authentication authentication
            ) {

        User currentUser =
                getCurrentUser(authentication);

        if (!isAccountant(currentUser)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ApiResponse.failure(
                                    "Only accountants can access this endpoint"
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Accountant profile fetched successfully",
                        toResponse(currentUser)
                )
        );
    }


    // =========================================================
    // GET ACCOUNTANT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountantResponse>>
            getAccountantById(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User accountant =
                getAccessibleAccountant(
                        id,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Accountant fetched successfully",
                        toResponse(accountant)
                )
        );
    }


    // =========================================================
    // UPDATE ACCOUNTANT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountantResponse>>
            updateAccountant(
                    @PathVariable Long id,
                    @Valid @RequestBody AccountantUpdateRequest request,
                    Authentication authentication
            ) {

        User accountant =
                getAccessibleAccountant(
                        id,
                        authentication
                );


        if (!accountant.getPhone().equals(
                request.getPhone()
        )) {

            if (accountantService
                    .findByPhone(request.getPhone())
                    .isPresent()) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                                ApiResponse.failure(
                                        "A user with this phone number already exists"
                                )
                        );
            }
        }


        accountant.setFirstName(
                request.getFirstName()
        );

        accountant.setLastName(
                request.getLastName()
        );

        accountant.setPhone(
                request.getPhone()
        );

        User updatedAccountant =
                accountantService.save(accountant);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Accountant updated successfully",
                        toResponse(updatedAccountant)
                )
        );
    }


    // =========================================================
    // FINANCE SUMMARY
    // =========================================================

    @GetMapping("/{id}/finance-summary")
    public ResponseEntity<
            ApiResponse<FinanceSummaryResponse>>
            getFinanceSummary(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User accountant =
                getAccessibleAccountant(
                        id,
                        authentication
                );

        validateOrganization(accountant);

        Long organizationId =
                accountant
                        .getOrganization()
                        .getId();


        List<FeeAccount> fees =
                accountantService
                        .findFeesByOrganization(
                                organizationId
                        );


        List<Payment> payments =
                accountantService
                        .findPaymentsByOrganization(
                                organizationId
                        );


        FinanceSummaryResponse response =
                buildFinanceSummary(
                        organizationId,
                        fees,
                        payments
                );


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Finance summary fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // ACCESS CONTROL
    // =========================================================

    private User getAccessibleAccountant(
            Long accountantId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        // SUPER ADMIN
        if ("SUPER_ADMIN".equals(role)) {

            return accountantService
                    .findAccountantById(
                            accountantId
                    )
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Accountant not found with id: "
                                            + accountantId
                            )
                    );
        }


        // ADMIN
        if ("ADMIN".equals(role)) {

            validateOrganization(currentUser);

            return accountantService
                    .findAccountantByIdAndOrganization(
                            accountantId,
                            currentUser
                                    .getOrganization()
                                    .getId()
                    )
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Accountant not found with id: "
                                            + accountantId
                            )
                    );
        }


        // ACCOUNTANT
        if ("ACCOUNTANT".equals(role)) {

            if (!currentUser.getId().equals(
                    accountantId
            )) {

                throw new IllegalArgumentException(
                        "Accountants can access only their own information"
                );
            }

            return currentUser;
        }


        throw new IllegalArgumentException(
                "You are not authorized to access accountant information"
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser(
            Authentication authentication
    ) {

        return userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Logged-in user not found"
                        )
                );
    }


    // =========================================================
    // ORGANIZATION VALIDATION
    // =========================================================

    private void validateOrganization(
            User user
    ) {

        if (user.getOrganization() == null) {

            throw new IllegalStateException(
                    "User is not assigned to an organization"
            );
        }
    }


    // =========================================================
    // ACCOUNTANT CHECK
    // =========================================================

    private boolean isAccountant(
            User user
    ) {

        return user.getRole() != null
                && "ACCOUNTANT".equals(
                        user.getRole().getName()
                );
    }


    // =========================================================
    // ACCOUNTANT RESPONSE
    // =========================================================

    private AccountantResponse toResponse(
            User accountant
    ) {

        return new AccountantResponse(
                accountant.getId(),
                accountant.getFirstName(),
                accountant.getLastName(),
                accountant.getEmail(),
                accountant.getPhone(),
                accountant.getStatus(),
                accountant.getOrganization() != null
                        ? accountant.getOrganization().getId()
                        : null
        );
    }


    // =========================================================
    // FINANCE SUMMARY CALCULATION
    // =========================================================

    private FinanceSummaryResponse buildFinanceSummary(
            Long organizationId,
            List<FeeAccount> fees,
            List<Payment> payments
    ) {

        FinanceSummaryResponse response =
                new FinanceSummaryResponse();

        response.setOrganizationId(
                organizationId
        );

        response.setTotalFeeAccounts(
                fees.size()
        );

        BigDecimal totalFees =
                BigDecimal.ZERO;

        BigDecimal totalDiscount =
                BigDecimal.ZERO;

        BigDecimal totalPayable =
                BigDecimal.ZERO;

        BigDecimal totalPaid =
                BigDecimal.ZERO;

        BigDecimal totalBalance =
                BigDecimal.ZERO;


        long pendingCount = 0;

        long partiallyPaidCount = 0;

        long paidCount = 0;


        for (FeeAccount fee : fees) {

            totalFees =
                    totalFees.add(
                            safe(
                                    fee.getTotalAmount()
                            )
                    );

            totalDiscount =
                    totalDiscount.add(
                            safe(
                                    fee.getDiscountAmount()
                            )
                    );

            totalPayable =
                    totalPayable.add(
                            safe(
                                    fee.getPayableAmount()
                            )
                    );

            totalPaid =
                    totalPaid.add(
                            safe(
                                    fee.getAmountPaid()
                            )
                    );

            totalBalance =
                    totalBalance.add(
                            safe(
                                    fee.getBalanceAmount()
                            )
                    );


            if (fee.getStatus()
                    == FeeStatus.PENDING) {

                pendingCount++;

            } else if (
                    fee.getStatus()
                            == FeeStatus.PARTIALLY_PAID
            ) {

                partiallyPaidCount++;

            } else if (
                    fee.getStatus()
                            == FeeStatus.PAID
            ) {

                paidCount++;
            }
        }


        BigDecimal successfulPaymentAmount =
                BigDecimal.ZERO;

        BigDecimal cancelledPaymentAmount =
                BigDecimal.ZERO;


        for (Payment payment : payments) {

            if (payment.getStatus()
                    == PaymentStatus.SUCCESS) {

                successfulPaymentAmount =
                        successfulPaymentAmount.add(
                                safe(
                                        payment.getAmount()
                                )
                        );

            } else if (
                    payment.getStatus()
                            == PaymentStatus.CANCELLED
            ) {

                cancelledPaymentAmount =
                        cancelledPaymentAmount.add(
                                safe(
                                        payment.getAmount()
                                )
                        );
            }
        }


        response.setTotalFees(totalFees);
        response.setTotalDiscount(totalDiscount);
        response.setTotalPayable(totalPayable);
        response.setTotalPaid(totalPaid);
        response.setTotalBalance(totalBalance);

        response.setPendingFeeAccounts(
                pendingCount
        );

        response.setPartiallyPaidFeeAccounts(
                partiallyPaidCount
        );

        response.setPaidFeeAccounts(
                paidCount
        );

        response.setTotalPayments(
                payments.size()
        );

        response.setSuccessfulPaymentAmount(
                successfulPaymentAmount
        );

        response.setCancelledPaymentAmount(
                cancelledPaymentAmount
        );

        return response;
    }


    private BigDecimal safe(
            BigDecimal value
    ) {

        return value != null
                ? value
                : BigDecimal.ZERO;
    }
}