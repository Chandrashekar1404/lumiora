package com.lumiora.batch.controller;

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

import com.lumiora.batch.dto.BatchCreateRequest;
import com.lumiora.batch.dto.BatchResponse;
import com.lumiora.batch.dto.BatchUpdateRequest;
import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.entity.BatchStatus;
import com.lumiora.batch.service.BatchService;

import com.lumiora.course.entity.Course;
import com.lumiora.course.service.CourseService;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;

import com.lumiora.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    private final UserService userService;

    private final CourseService courseService;

    private final OrganizationRepository organizationRepository;

    // =========================================================
    // CREATE BATCH
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createBatch(
            @Valid @RequestBody BatchCreateRequest request,
            Authentication authentication) {

        boolean isSuperAdmin = isSuperAdmin(authentication);

        if (batchService.existsByCodeAndOrganizationId(
                request.getCode(),
                request.getOrganizationId())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "A batch with this code already exists in this organization"));
        }

        Organization organization;

        if (isSuperAdmin) {

            organization = organizationRepository
                    .findById(request.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Organization not found: "
                                    + request.getOrganizationId()));

        } else {

            User currentUser = getCurrentUser(authentication);

            organization = validateAdminOrganization(
                    currentUser,
                    request.getOrganizationId());
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date");
        }

        if (!request.getEndTime()
                .isAfter(request.getStartTime())) {

            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        Course course = courseService
                .findByIdAndOrganizationId(
                        request.getCourseId(),
                        organization.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course not found in this organization: "
                                + request.getCourseId()));

        User trainer = null;

        if (request.getTrainerId() != null) {

            trainer = validateTrainer(
                    request.getTrainerId(),
                    organization.getId());
        }

        Batch batch = new Batch();

        batch.setName(request.getName());
        batch.setCode(request.getCode());
        batch.setDescription(request.getDescription());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setSchedule(request.getSchedule());
        batch.setStartTime(request.getStartTime());
        batch.setEndTime(request.getEndTime());
        batch.setCapacity(request.getCapacity());
        batch.setStatus(BatchStatus.UPCOMING);
        batch.setActive(true);

        batch.setOrganization(organization);
        batch.setCourse(course);
        batch.setTrainer(trainer);

        Batch savedBatch = batchService.save(batch);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Batch created successfully",
                                toResponse(savedBatch)));
    }

    // =========================================================
    // GET ALL BATCHES
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getAllBatches(
            Authentication authentication) {

        List<Batch> batches;

        if (isSuperAdmin(authentication)) {

            batches = batchService.findAll();

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization");
            }

            Long organizationId = currentUser.getOrganization().getId();

            batches = batchService.findAllByOrganizationId(
                    organizationId);
        }

        List<BatchResponse> responses = batches.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batches fetched successfully",
                        responses));
    }

    // =========================================================
    // GET BATCH BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatchById(
            @PathVariable Long id,
            Authentication authentication) {

        Batch batch;

        if (isSuperAdmin(authentication)) {

            batch = batchService
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Batch not found with id: " + id));

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization");
            }

            Long organizationId = currentUser.getOrganization().getId();

            batch = batchService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Batch not found with id: " + id));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch fetched successfully",
                        toResponse(batch)));
    }

    // =========================================================
    // UPDATE BATCH
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody BatchUpdateRequest request,
            Authentication authentication) {

        Batch batch;

        if (isSuperAdmin(authentication)) {

            batch = batchService
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Batch not found with id: " + id));

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization");
            }

            Long organizationId = currentUser.getOrganization().getId();

            batch = batchService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Batch not found with id: " + id));
        }

        Long organizationId = batch.getOrganization().getId();

        // =====================================================
        // BASIC FIELD UPDATES
        // =====================================================

        if (request.getName() != null &&
                !request.getName().isBlank()) {

            batch.setName(request.getName());
        }

        if (request.getDescription() != null) {

            batch.setDescription(
                    request.getDescription());
        }

        if (request.getStartDate() != null) {

            batch.setStartDate(
                    request.getStartDate());
        }

        if (request.getEndDate() != null) {

            batch.setEndDate(
                    request.getEndDate());
        }

        if (request.getSchedule() != null) {

            batch.setSchedule(
                    request.getSchedule());
        }

        if (request.getStartTime() != null) {

            batch.setStartTime(
                    request.getStartTime());
        }

        if (request.getEndTime() != null) {

            batch.setEndTime(
                    request.getEndTime());
        }

        if (request.getCapacity() != null) {

            batch.setCapacity(
                    request.getCapacity());
        }

        // =====================================================
        // DATE/TIME VALIDATION
        // =====================================================

        if (batch.getEndDate()
                .isBefore(batch.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date");
        }

        if (!batch.getEndTime()
                .isAfter(batch.getStartTime())) {

            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        // =====================================================
        // COURSE UPDATE
        // =====================================================

        if (request.getCourseId() != null) {

            Course course = courseService
                    .findByIdAndOrganizationId(
                            request.getCourseId(),
                            organizationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Course not found in this organization: "
                                    + request.getCourseId()));

            batch.setCourse(course);
        }

        // =====================================================
        // TRAINER UPDATE
        // =====================================================

        if (request.getTrainerId() != null) {

            User trainer = validateTrainer(
                    request.getTrainerId(),
                    organizationId);

            batch.setTrainer(trainer);
        }

        // =====================================================
        // STATUS
        // =====================================================

        if (request.getStatus() != null) {

            batch.setStatus(
                    request.getStatus());
        }

        // =====================================================
        // ACTIVE
        // =====================================================

        if (request.getActive() != null) {

            batch.setActive(
                    request.getActive());
        }

        batchService.save(batch);

        Batch updatedBatch = batchService
                .findById(batch.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Batch not found with id: " + batch.getId()));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch updated successfully",
                        toResponse(updatedBatch)));
    }

    // =========================================================
    // DEACTIVATE BATCH
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateBatch(
            @PathVariable Long id,
            Authentication authentication) {

        Batch batch;

        if (isSuperAdmin(authentication)) {

            batch = batchService
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Batch not found with id: " + id));

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "Admin is not assigned to an organization");
            }

            Long organizationId = currentUser.getOrganization().getId();

            batch = batchService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Batch not found with id: " + id));
        }

        batch.setActive(false);

        batchService.save(batch);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch deactivated successfully",
                        null));
    }

    // =========================================================
    // VALIDATE TRAINER
    // =========================================================

    private User validateTrainer(
            Long trainerId,
            Long organizationId) {

        User trainer = userService
                .findByIdAndOrganizationId(
                        trainerId,
                        organizationId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Trainer not found in this organization: "
                                + trainerId));

        if (trainer.getRole() == null ||
                !"TRAINER".equals(
                        trainer.getRole().getName())) {

            throw new IllegalArgumentException(
                    "Selected user is not a TRAINER");
        }

        if (trainer.getStatus() != UserStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Trainer is not active");
        }

        return trainer;
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
    // ADMIN ORGANIZATION VALIDATION
    // =========================================================

    private Organization validateAdminOrganization(
            User currentUser,
            Long requestedOrganizationId) {

        if (currentUser.getOrganization() == null) {

            throw new IllegalStateException(
                    "Admin is not assigned to an organization");
        }

        Long currentOrganizationId = currentUser.getOrganization().getId();

        if (!currentOrganizationId.equals(
                requestedOrganizationId)) {

            throw new IllegalArgumentException(
                    "You cannot create a batch for another organization");
        }

        return currentUser.getOrganization();
    }

    // =========================================================
    // SUPER ADMIN CHECK
    // =========================================================

    private boolean isSuperAdmin(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority()
                        .equals("ROLE_SUPER_ADMIN"));
    }

    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private BatchResponse toResponse(Batch batch) {

        Long trainerId = null;

        String trainerName = null;

        if (batch.getTrainer() != null) {

            trainerId = batch.getTrainer().getId();

            trainerName = (batch.getTrainer().getFirstName()
                    + " "
                    + (batch.getTrainer().getLastName() != null
                            ? batch.getTrainer().getLastName()
                            : ""))
                    .trim();
        }

        return new BatchResponse(
                batch.getId(),
                batch.getName(),
                batch.getCode(),
                batch.getDescription(),
                batch.getStartDate(),
                batch.getEndDate(),
                batch.getSchedule(),
                batch.getStartTime(),
                batch.getEndTime(),
                batch.getCapacity(),
                batch.getStatus(),
                batch.isActive(),
                batch.getOrganization() != null
                        ? batch.getOrganization().getId()
                        : null,
                batch.getCourse() != null
                        ? batch.getCourse().getId()
                        : null,
                batch.getCourse() != null
                        ? batch.getCourse().getName()
                        : null,
                trainerId,
                trainerName);
    }
}