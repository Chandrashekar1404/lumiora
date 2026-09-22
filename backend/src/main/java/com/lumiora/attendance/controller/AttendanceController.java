package com.lumiora.attendance.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.attendance.dto.AttendanceCreateRequest;
import com.lumiora.attendance.dto.AttendanceResponse;
import com.lumiora.attendance.dto.AttendanceUpdateRequest;
import com.lumiora.attendance.entity.Attendance;
import com.lumiora.attendance.service.AttendanceService;

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.service.BatchService;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.enrollment.service.EnrollmentService;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    private final UserService userService;

    private final BatchService batchService;

    private final EnrollmentService enrollmentService;


    // =========================================================
    // CREATE ATTENDANCE
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createAttendance(
            @Valid @RequestBody AttendanceCreateRequest request,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

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

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
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
                                    "Batch not found in your organization: "
                                            + request.getBatchId()
                            ));

            student = userService
                    .findByIdAndOrganizationId(
                            request.getStudentId(),
                            organizationId
                    )
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Student not found in your organization: "
                                            + request.getStudentId()
                            ));
        }


        // =====================================================
        // TRAINER ACCESS CHECK
        // =====================================================

        if (!isSuperAdmin &&
                hasRole(authentication, "TRAINER")) {

            if (batch.getTrainer() == null ||
                    !batch.getTrainer()
                            .getId()
                            .equals(currentUser.getId())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                ApiResponse.failure(
                                        "Trainer is not assigned to this batch"
                                )
                        );
            }
        }


        // =====================================================
        // STUDENT VALIDATION
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
        // ORGANIZATION VALIDATION
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
        // BATCH DATE VALIDATION
        // =====================================================

        if (request.getAttendanceDate()
                .isBefore(batch.getStartDate()) ||
                request.getAttendanceDate()
                        .isAfter(batch.getEndDate())) {

            throw new IllegalArgumentException(
                    "Attendance date must be within the batch duration"
            );
        }


        // =====================================================
        // ENROLLMENT VALIDATION
        // =====================================================

        if (!enrollmentService
                .existsByStudentAndBatch(
                        student.getId(),
                        batch.getId()
                )) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.failure(
                                    "Student is not enrolled in this batch"
                            )
                    );
        }


        // =====================================================
        // DUPLICATE ATTENDANCE
        // =====================================================

        if (attendanceService
                .existsByStudentAndBatchAndDate(
                        student.getId(),
                        batch.getId(),
                        request.getAttendanceDate()
                )) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "Attendance already exists for this student on this date"
                            )
                    );
        }


        // =====================================================
        // CREATE ATTENDANCE
        // =====================================================

        Attendance attendance =
                new Attendance();

        attendance.setAttendanceDate(
                request.getAttendanceDate()
        );

        attendance.setStatus(
                request.getStatus()
        );

        attendance.setRemarks(
                request.getRemarks()
        );

        attendance.setOrganization(
                batch.getOrganization()
        );

        attendance.setBatch(batch);

        attendance.setStudent(student);

        Attendance savedAttendance =
                attendanceService.save(attendance);


        // Reload so EntityGraph initializes relationships
        Attendance refreshedAttendance =
                attendanceService
                        .findById(savedAttendance.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Attendance not found after creation"
                                ));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Attendance marked successfully",
                                toResponse(refreshedAttendance)
                        )
                );
    }


    // =========================================================
    // GET ALL ATTENDANCE
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>>
            getAllAttendance(
                    Authentication authentication) {

        List<Attendance> attendanceList;

        if (isSuperAdmin(authentication)) {

            attendanceList =
                    attendanceService.findAll();

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

            attendanceList =
                    attendanceService
                            .findAllByOrganizationId(
                                    organizationId
                            );
        }

        List<AttendanceResponse> responses =
                attendanceList.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET ATTENDANCE BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>>
            getAttendanceById(
                    @PathVariable Long id,
                    Authentication authentication) {

        Attendance attendance;

        if (isSuperAdmin(authentication)) {

            attendance =
                    attendanceService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Attendance not found with id: "
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

            Long organizationId =
                    currentUser.getOrganization().getId();

            attendance =
                    attendanceService
                            .findByIdAndOrganizationId(
                                    id,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Attendance not found with id: "
                                                    + id
                                    ));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance fetched successfully",
                        toResponse(attendance)
                )
        );
    }


    // =========================================================
    // GET ATTENDANCE BY DATE
    // =========================================================

    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>>
            getAttendanceByDate(
                    @RequestParam LocalDate date,
                    Authentication authentication) {

        List<Attendance> attendanceList;

        if (isSuperAdmin(authentication)) {

            // For SUPER_ADMIN, return all organizations
            attendanceList =
                    attendanceService
                            .findAll()
                            .stream()
                            .filter(attendance ->
                                    attendance
                                            .getAttendanceDate()
                                            .equals(date)
                            )
                            .toList();

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

            attendanceList =
                    attendanceService
                            .findAllByOrganizationAndDate(
                                    organizationId,
                                    date
                            );
        }

        List<AttendanceResponse> responses =
                attendanceList.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET ATTENDANCE BY BATCH
    // =========================================================

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>>
            getAttendanceByBatch(
                    @PathVariable Long batchId,
                    Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        List<Attendance> attendanceList;

        if (isSuperAdmin(authentication)) {

            // Validate that batch exists
            batchService
                    .findById(batchId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Batch not found with id: "
                                            + batchId
                            ));

            attendanceList =
                    attendanceService
                            .findAll()
                            .stream()
                            .filter(attendance ->
                                    attendance.getBatch()
                                            .getId()
                                            .equals(batchId))
                            .toList();

        } else {

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            // Validate batch belongs to current organization
            batchService
                    .findByIdAndOrganizationId(
                            batchId,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Batch not found in your organization: "
                                            + batchId
                            ));

            // Trainer can only view assigned batch attendance
            if (hasRole(authentication, "TRAINER")) {

                Batch batch =
                        batchService
                                .findByIdAndOrganizationId(
                                        batchId,
                                        organizationId
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Batch not found"
                                        ));

                if (batch.getTrainer() == null ||
                        !batch.getTrainer()
                                .getId()
                                .equals(currentUser.getId())) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                    ApiResponse.failure(
                                            "Trainer is not assigned to this batch"
                                    )
                            );
                }
            }

            attendanceList =
                    attendanceService
                            .findAllByOrganizationAndBatch(
                                    organizationId,
                                    batchId
                            );
        }

        List<AttendanceResponse> responses =
                attendanceList.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch attendance fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // UPDATE ATTENDANCE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        Attendance attendance;

        if (isSuperAdmin(authentication)) {

            attendance =
                    attendanceService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Attendance not found with id: "
                                                    + id
                                    ));

        } else {

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            attendance =
                    attendanceService
                            .findByIdAndOrganizationId(
                                    id,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Attendance not found with id: "
                                                    + id
                                    ));

            // Trainer can modify only their own batch attendance
            if (hasRole(authentication, "TRAINER")) {

                if (attendance.getBatch().getTrainer() == null ||
                        !attendance.getBatch()
                                .getTrainer()
                                .getId()
                                .equals(currentUser.getId())) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                    ApiResponse.failure(
                                            "Trainer is not assigned to this batch"
                                    )
                            );
                }
            }
        }


        attendance.setStatus(
                request.getStatus()
        );

        attendance.setRemarks(
                request.getRemarks()
        );

        attendanceService.save(attendance);

        Attendance refreshedAttendance =
                attendanceService
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Attendance not found after update"
                                ));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance updated successfully",
                        toResponse(refreshedAttendance)
                )
        );
    }


    // =========================================================
    // DELETE / CANCEL ATTENDANCE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deleteAttendance(
                    @PathVariable Long id,
                    Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        Attendance attendance;

        if (isSuperAdmin(authentication)) {

            attendance =
                    attendanceService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Attendance not found with id: "
                                                    + id
                                    ));

        } else {

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            attendance =
                    attendanceService
                            .findByIdAndOrganizationId(
                                    id,
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Attendance not found with id: "
                                                    + id
                                    ));

            if (hasRole(authentication, "TRAINER")) {

                if (attendance.getBatch().getTrainer() == null ||
                        !attendance.getBatch()
                                .getTrainer()
                                .getId()
                                .equals(currentUser.getId())) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                    ApiResponse.failure(
                                            "Trainer is not assigned to this batch"
                                    )
                            );
                }
            }
        }

        attendanceService.save(attendance);

        attendanceService
                .findById(id)
                .ifPresent(found -> {
                    // Intentionally retained for response consistency
                });

        /*
         * Attendance is operational data, so we use a hard delete here.
         * This will be replaced by a dedicated attendance correction/audit
         * mechanism later when we build audit logging.
         */
        attendanceService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance deleted successfully",
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
                        )
                );
    }


    // =========================================================
    // SUPER ADMIN CHECK
    // =========================================================

    private boolean isSuperAdmin(
            Authentication authentication) {

        return hasRole(
                authentication,
                "SUPER_ADMIN"
        );
    }


    // =========================================================
    // ROLE CHECK
    // =========================================================

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals(
                                        "ROLE_" + role
                                )
                );
    }


    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private AttendanceResponse toResponse(
            Attendance attendance) {

        User student =
                attendance.getStudent();

        Batch batch =
                attendance.getBatch();

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

        return new AttendanceResponse(
                attendance.getId(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getRemarks(),

                attendance.getOrganization() != null
                        ? attendance.getOrganization().getId()
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