package com.lumiora.fee.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.service.EnrollmentService;

import com.lumiora.entity.auth.User;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.fee.dto.FeeCreateRequest;
import com.lumiora.fee.dto.FeeResponse;
import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.FeeStatus;
import com.lumiora.fee.service.FeeService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    private final EnrollmentService enrollmentService;
    private final com.lumiora.service.UserService userService;

    // =========================================================
    // CREATE FEE ACCOUNT
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createFee(
            @Valid @RequestBody FeeCreateRequest request,
            Authentication authentication) {

        boolean isSuperAdmin = isSuperAdmin(authentication);

        Enrollment enrollment;

        Long organizationId;

        if (isSuperAdmin) {

            enrollment = enrollmentService
                    .findById(request.getEnrollmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Enrollment not found with id: "
                                    + request.getEnrollmentId()));

            organizationId = enrollment.getOrganization()
                    .getId();

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization");
            }

            organizationId = currentUser.getOrganization().getId();

            enrollment = enrollmentService
                    .findByIdAndOrganizationId(
                            request.getEnrollmentId(),
                            organizationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Enrollment not found in your organization: "
                                    + request.getEnrollmentId()));
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED ||
                enrollment.getStatus() == EnrollmentStatus.DROPPED) {

            throw new IllegalArgumentException(
                    "Cannot create fees for a cancelled or dropped enrollment");
        }

        if (feeService
                .existsByEnrollmentAndOrganization(
                        enrollment.getId(),
                        organizationId)) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "A fee account already exists for this enrollment"));
        }

        BigDecimal totalAmount = request.getTotalAmount();

        BigDecimal discountAmount = request.getDiscountAmount() != null
                ? request.getDiscountAmount()
                : BigDecimal.ZERO;

        if (discountAmount.compareTo(
                totalAmount) > 0) {

            throw new IllegalArgumentException(
                    "Discount cannot exceed total amount");
        }

        BigDecimal payableAmount = totalAmount.subtract(
                discountAmount);

        FeeAccount feeAccount = new FeeAccount();

        feeAccount.setEnrollment(
                enrollment);

        feeAccount.setOrganization(
                enrollment.getOrganization());

        feeAccount.setTotalAmount(
                totalAmount);

        feeAccount.setDiscountAmount(
                discountAmount);

        feeAccount.setPayableAmount(
                payableAmount);

        feeAccount.setAmountPaid(
                BigDecimal.ZERO);

        feeAccount.setBalanceAmount(
                payableAmount);

        feeAccount.setDueDate(
                request.getDueDate());

        feeAccount.setStatus(
                payableAmount.compareTo(
                        BigDecimal.ZERO) == 0
                                ? FeeStatus.PAID
                                : FeeStatus.PENDING);

        feeAccount.setActive(true);

        FeeAccount savedFee = feeService.save(feeAccount);

        FeeAccount refreshedFee = feeService
                .findById(savedFee.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Fee account not found after creation"));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Fee account created successfully",
                                toResponse(refreshedFee)));
    }

    // =========================================================
    // GET ALL FEE ACCOUNTS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeeResponse>>> getAllFees(
            Authentication authentication) {

        List<FeeAccount> fees;

        if (isSuperAdmin(authentication)) {

            fees = feeService.findAll();

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization");
            }

            Long organizationId = currentUser.getOrganization().getId();

            fees = feeService.findAllByOrganizationId(
                    organizationId);
        }

        List<FeeResponse> responses = fees.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee accounts fetched successfully",
                        responses));
    }

    // =========================================================
    // GET FEE BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeResponse>> getFeeById(
            @PathVariable Long id,
            Authentication authentication) {

        FeeAccount feeAccount;

        if (isSuperAdmin(authentication)) {

            feeAccount = feeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Fee account not found with id: "
                                    + id));

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization");
            }

            Long organizationId = currentUser.getOrganization().getId();

            feeAccount = feeService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Fee account not found with id: "
                                    + id));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee account fetched successfully",
                        toResponse(feeAccount)));
    }

    // =========================================================
    // DEACTIVATE FEE ACCOUNT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateFee(
            @PathVariable Long id,
            Authentication authentication) {

        FeeAccount feeAccount;

        if (isSuperAdmin(authentication)) {

            feeAccount = feeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Fee account not found with id: "
                                    + id));

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization");
            }

            Long organizationId = currentUser.getOrganization().getId();

            feeAccount = feeService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Fee account not found with id: "
                                    + id));
        }

        if (feeAccount.getAmountPaid()
                .compareTo(BigDecimal.ZERO) > 0) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "Cannot deactivate a fee account with recorded payments"));
        }

        feeAccount.setActive(false);
        feeAccount.setStatus(
                FeeStatus.CANCELLED);

        feeService.save(feeAccount);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee account deactivated successfully",
                        null));
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser(
            Authentication authentication) {

        return userService
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(
                        "Logged-in user not found"));
    }

    // =========================================================
    // SUPER ADMIN
    // =========================================================

    private boolean isSuperAdmin(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority()
                        .equals("ROLE_SUPER_ADMIN"));
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private FeeResponse toResponse(
            FeeAccount feeAccount) {

        Enrollment enrollment = feeAccount.getEnrollment();

        User student = enrollment.getStudent();

        String studentName = (student.getFirstName()
                + " "
                + (student.getLastName() != null
                        ? student.getLastName()
                        : ""))
                .trim();

        return new FeeResponse(
                feeAccount.getId(),
                enrollment.getId(),
                feeAccount.getOrganization().getId(),
                student.getId(),
                studentName,
                student.getEmail(),
                enrollment.getBatch().getId(),
                enrollment.getBatch().getName(),
                enrollment.getBatch().getCode(),
                enrollment.getBatch().getCourse().getName(),
                feeAccount.getTotalAmount(),
                feeAccount.getDiscountAmount(),
                feeAccount.getPayableAmount(),
                feeAccount.getAmountPaid(),
                feeAccount.getBalanceAmount(),
                feeAccount.getDueDate(),
                feeAccount.getStatus(),
                feeAccount.isActive());
    }

    // =========================================================
    // TEMPORARY ACCESS WRAPPER
    // =========================================================

    private static class UserServiceAccess {

        private final Authentication authentication;

        private UserServiceAccess(
                Authentication authentication) {

            this.authentication = authentication;
        }

        private User getUser() {

            throw new UnsupportedOperationException(
                    "Replace UserServiceAccess with UserService injection");
        }
    }
}