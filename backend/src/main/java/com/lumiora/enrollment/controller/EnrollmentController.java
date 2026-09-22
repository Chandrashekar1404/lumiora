package com.lumiora.enrollment.controller;

import java.time.LocalDate;
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

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.entity.BatchStatus;
import com.lumiora.batch.service.BatchService;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.enrollment.dto.EnrollmentCreateRequest;
import com.lumiora.enrollment.dto.EnrollmentResponse;
import com.lumiora.enrollment.dto.EnrollmentUpdateRequest;
import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.service.EnrollmentService;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.service.UserService;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    private final UserService userService;

    private final BatchService batchService;


    // =========================================================
    // CREATE ENROLLMENT
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createEnrollment(
            @Valid @RequestBody EnrollmentCreateRequest request,
            Authentication authentication) {

        boolean isSuperAdmin =
                isSuperAdmin(authentication);

        Batch batch;

        User student;

        if (isSuperAdmin) {

            batch = batchService
                    .findById(request.getBatchId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Batch not found with id: "
                                            + request.getBatchId()
                            ));

            student = userService
                    .findById(request.getStudentId())
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Student not found with id: "
                                            + request.getStudentId()
                            ));

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {
                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            batch = batchService
                    .findByIdAndOrganizationId(
                            request.getBatchId(),
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Batch not found in this organization: "
                                            + request.getBatchId()
                            ));

            student = userService
                    .findByIdAndOrganizationId(
                            request.getStudentId(),
                            organizationId
                    )
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Student not found in this organization: "
                                            + request.getStudentId()
                            ));
        }


        // =====================================================
        // VALIDATE STUDENT
        // =====================================================

        if (student.getRole() == null ||
                !"STUDENT".equals(
                        student.getRole().getName())) {

            throw new IllegalArgumentException(
                    "Selected user is not a STUDENT"
            );
        }

        if (student.getStatus() != UserStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Student is not active"
            );
        }


        // =====================================================
        // VALIDATE ORGANIZATION MATCH
        // =====================================================

        if (batch.getOrganization() == null ||
                student.getOrganization() == null) {

            throw new IllegalStateException(
                    "Batch and student must belong to an organization"
            );
        }

        Long batchOrganizationId =
                batch.getOrganization().getId();

        Long studentOrganizationId =
                student.getOrganization().getId();

        if (!batchOrganizationId.equals(
                studentOrganizationId)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ApiResponse.failure(
                                    "Student and batch must belong to the same organization"
                            )
                    );
        }


        // =====================================================
        // VALIDATE BATCH
        // =====================================================

        if (!batch.isActive()) {

            throw new IllegalArgumentException(
                    "Batch is inactive"
            );
        }

        if (batch.getStatus() == BatchStatus.COMPLETED ||
                batch.getStatus() == BatchStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot enroll a student in a completed or cancelled batch"
            );
        }


        // =====================================================
        // DUPLICATE ENROLLMENT
        // =====================================================

        if (enrollmentService.existsByStudentAndBatch(
                student.getId(),
                batch.getId())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "Student is already enrolled in this batch"
                            )
                    );
        }


        // =====================================================
        // CAPACITY CHECK
        // =====================================================

        long enrolledCount =
                enrollmentService.countByBatchAndStatus(
                        batch.getId(),
                        EnrollmentStatus.ENROLLED
                );

        if (enrolledCount >= batch.getCapacity()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "Batch capacity is full"
                            )
                    );
        }


        // =====================================================
        // CREATE
        // =====================================================

        Enrollment enrollment =
                new Enrollment();

        enrollment.setEnrollmentDate(
                LocalDate.now()
        );

        enrollment.setStatus(
                EnrollmentStatus.ENROLLED
        );

        enrollment.setNotes(
                request.getNotes()
        );

        enrollment.setOrganization(
                batch.getOrganization()
        );

        enrollment.setBatch(batch);

        enrollment.setStudent(student);

        Enrollment savedEnrollment =
                enrollmentService.save(enrollment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Student enrolled successfully",
                                toResponse(savedEnrollment)
                        )
                );
    }


    // =========================================================
    // GET ALL ENROLLMENTS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>>
            getAllEnrollments(
                    Authentication authentication) {

        List<Enrollment> enrollments;

        if (isSuperAdmin(authentication)) {

            enrollments =
                    enrollmentService.findAll();

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            enrollments =
                    enrollmentService
                            .findAllByOrganizationId(
                                    organizationId
                            );
        }

        List<EnrollmentResponse> responses =
                enrollments.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Enrollments fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET ENROLLMENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>>
            getEnrollmentById(
                    @PathVariable Long id,
                    Authentication authentication) {

        Enrollment enrollment;

        if (isSuperAdmin(authentication)) {

            enrollment =
                    enrollmentService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Enrollment not found with id: "
                                                    + id
                                    ));

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            enrollment =
                    enrollmentService
                            .findByIdAndOrganizationId(
                                    id,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Enrollment not found with id: "
                                                    + id
                                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Enrollment fetched successfully",
                        toResponse(enrollment)
                )
        );
    }


    // =========================================================
    // UPDATE ENROLLMENT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>>
            updateEnrollment(
                    @PathVariable Long id,
                    @Valid @RequestBody EnrollmentUpdateRequest request,
                    Authentication authentication) {

        Enrollment enrollment;

        if (isSuperAdmin(authentication)) {

            enrollment =
                    enrollmentService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Enrollment not found with id: "
                                                    + id
                                    ));

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            enrollment =
                    enrollmentService
                            .findByIdAndOrganizationId(
                                    id,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Enrollment not found with id: "
                                                    + id
                                    ));
        }


        // =====================================================
        // STATUS UPDATE
        // =====================================================

        if (request.getStatus() != null) {

            if (request.getStatus()
                    == EnrollmentStatus.ENROLLED &&
                    enrollment.getStatus()
                            != EnrollmentStatus.ENROLLED) {

                long enrolledCount =
                        enrollmentService
                                .countByBatchAndStatus(
                                        enrollment.getBatch().getId(),
                                        EnrollmentStatus.ENROLLED
                                );

                if (enrolledCount >=
                        enrollment.getBatch().getCapacity()) {

                    throw new IllegalArgumentException(
                            "Batch capacity is full"
                    );
                }
            }

            enrollment.setStatus(
                    request.getStatus()
            );
        }


        // =====================================================
        // NOTES
        // =====================================================

        if (request.getNotes() != null) {

            enrollment.setNotes(
                    request.getNotes()
            );
        }

        Enrollment updatedEnrollment =
                enrollmentService.save(enrollment);

        Enrollment refreshedEnrollment =
                enrollmentService
                        .findById(updatedEnrollment.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Enrollment not found after update"
                                ));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Enrollment updated successfully",
                        toResponse(refreshedEnrollment)
                )
        );
    }


    // =========================================================
    // CANCEL ENROLLMENT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            cancelEnrollment(
                    @PathVariable Long id,
                    Authentication authentication) {

        Enrollment enrollment;

        if (isSuperAdmin(authentication)) {

            enrollment =
                    enrollmentService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Enrollment not found with id: "
                                                    + id
                                    ));

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            enrollment =
                    enrollmentService
                            .findByIdAndOrganizationId(
                                    id,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Enrollment not found with id: "
                                                    + id
                                    ));
        }

        enrollment.setStatus(
                EnrollmentStatus.CANCELLED
        );

        enrollmentService.save(enrollment);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Enrollment cancelled successfully",
                        null
                )
        );
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
                        ));
    }


    // =========================================================
    // SUPER ADMIN CHECK
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
    // RESPONSE MAPPING
    // =========================================================

    private EnrollmentResponse toResponse(
            Enrollment enrollment) {

        User student =
                enrollment.getStudent();

        Batch batch =
                enrollment.getBatch();

        String studentName =
                (student.getFirstName()
                        + " "
                        + (
                        student.getLastName() != null
                                ? student.getLastName()
                                : ""
                )).trim();

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrollmentDate(),
                enrollment.getStatus(),
                enrollment.getNotes(),
                enrollment.getOrganization() != null
                        ? enrollment.getOrganization().getId()
                        : null,
                batch != null
                        ? batch.getId()
                        : null,
                batch != null
                        ? batch.getName()
                        : null,
                batch != null
                        ? batch.getCode()
                        : null,
                student != null
                        ? student.getId()
                        : null,
                studentName,
                student != null
                        ? student.getEmail()
                        : null
        );
    }
}