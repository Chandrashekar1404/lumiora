package com.lumiora.trainer.controller;

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

import com.lumiora.attendance.dto.AttendanceResponse;
import com.lumiora.attendance.entity.Attendance;

import com.lumiora.batch.dto.BatchResponse;
import com.lumiora.batch.entity.Batch;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.entity.auth.User;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.service.UserService;

import com.lumiora.trainer.dto.TrainerResponse;
import com.lumiora.trainer.dto.TrainerUpdateRequest;
import com.lumiora.trainer.service.TrainerService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    private final UserService userService;


    // =========================================================
    // GET ALL TRAINERS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainerResponse>>>
            getAllTrainers(
                    Authentication authentication
            ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();

        List<User> trainers;


        // SUPER ADMIN
        if ("SUPER_ADMIN".equals(role)) {

            trainers =
                    trainerService.findAllTrainers();

        }


        // ADMIN
        else if ("ADMIN".equals(role)) {

            validateOrganization(currentUser);

            trainers =
                    trainerService
                            .findAllTrainersByOrganization(
                                    currentUser
                                            .getOrganization()
                                            .getId()
                            );

        }


        // TRAINER
        else if ("TRAINER".equals(role)) {

            trainers =
                    List.of(currentUser);

        }


        else {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ApiResponse.failure(
                                    "You are not authorized to view trainers"
                            )
                    );
        }


        List<TrainerResponse> responses =
                trainers.stream()
                        .map(this::toTrainerResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainers fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET MY PROFILE
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TrainerResponse>>
            getMyProfile(
                    Authentication authentication
            ) {

        User currentUser =
                getCurrentUser(authentication);

        if (!isTrainer(currentUser)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ApiResponse.failure(
                                    "Only trainers can access this endpoint"
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainer profile fetched successfully",
                        toTrainerResponse(currentUser)
                )
        );
    }


    // =========================================================
    // GET TRAINER BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerResponse>>
            getTrainerById(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User trainer =
                getAccessibleTrainer(
                        id,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainer fetched successfully",
                        toTrainerResponse(trainer)
                )
        );
    }


    // =========================================================
    // GET TRAINER BATCHES
    // =========================================================

    @GetMapping("/{id}/batches")
    public ResponseEntity<ApiResponse<List<BatchResponse>>>
            getTrainerBatches(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User trainer =
                getAccessibleTrainer(
                        id,
                        authentication
                );

        validateOrganization(trainer);

        List<Batch> batches =
                trainerService.findTrainerBatches(
                        trainer.getOrganization().getId(),
                        trainer.getId()
                );

        List<BatchResponse> responses =
                batches.stream()
                        .map(this::toBatchResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainer batches fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET TRAINER ATTENDANCE
    // =========================================================

    @GetMapping("/{id}/attendance")
    public ResponseEntity<
            ApiResponse<List<AttendanceResponse>>>
            getTrainerAttendance(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User trainer =
                getAccessibleTrainer(
                        id,
                        authentication
                );

        validateOrganization(trainer);

        List<Attendance> attendance =
                trainerService.findTrainerAttendance(
                        trainer.getOrganization().getId(),
                        trainer.getId()
                );

        List<AttendanceResponse> responses =
                attendance.stream()
                        .map(this::toAttendanceResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainer attendance fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // UPDATE TRAINER
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerResponse>>
            updateTrainer(
                    @PathVariable Long id,
                    @Valid @RequestBody TrainerUpdateRequest request,
                    Authentication authentication
            ) {

        User trainer =
                getAccessibleTrainer(
                        id,
                        authentication
                );


        // -----------------------------------------------------
        // PHONE UNIQUENESS
        // -----------------------------------------------------

        if (!trainer.getPhone().equals(
                request.getPhone()
        )) {

            if (trainerService
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


        trainer.setFirstName(
                request.getFirstName()
        );

        trainer.setLastName(
                request.getLastName()
        );

        trainer.setPhone(
                request.getPhone()
        );

        User updatedTrainer =
                trainerService.save(trainer);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainer updated successfully",
                        toTrainerResponse(updatedTrainer)
                )
        );
    }


    // =========================================================
    // ACCESS CONTROL
    // =========================================================

    private User getAccessibleTrainer(
            Long trainerId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        // SUPER ADMIN
        if ("SUPER_ADMIN".equals(role)) {

            return trainerService
                    .findTrainerById(trainerId)
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Trainer not found with id: "
                                            + trainerId
                            ));
        }


        // ADMIN
        if ("ADMIN".equals(role)) {

            validateOrganization(currentUser);

            return trainerService
                    .findTrainerByIdAndOrganization(
                            trainerId,
                            currentUser
                                    .getOrganization()
                                    .getId()
                    )
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Trainer not found with id: "
                                            + trainerId
                            ));
        }


        // TRAINER
        if ("TRAINER".equals(role)) {

            if (!currentUser.getId().equals(
                    trainerId
            )) {

                throw new IllegalArgumentException(
                        "Trainers can access only their own information"
                );
            }

            return currentUser;
        }


        throw new IllegalArgumentException(
                "You are not authorized to access trainer information"
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
                        ));
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
    // TRAINER CHECK
    // =========================================================

    private boolean isTrainer(
            User user
    ) {

        return user.getRole() != null
                && "TRAINER".equals(
                        user.getRole().getName()
                );
    }


    // =========================================================
    // TRAINER RESPONSE
    // =========================================================

    private TrainerResponse toTrainerResponse(
            User trainer
    ) {

        return new TrainerResponse(
                trainer.getId(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getEmail(),
                trainer.getPhone(),
                trainer.getStatus(),
                trainer.getOrganization() != null
                        ? trainer.getOrganization().getId()
                        : null
        );
    }


    // =========================================================
    // BATCH RESPONSE
    // =========================================================

    private BatchResponse toBatchResponse(
            Batch batch
    ) {

        String trainerName = null;

        if (batch.getTrainer() != null) {

            trainerName =
                    batch.getTrainer().getFirstName()
                            + " "
                            + (
                            batch.getTrainer().getLastName() != null
                                    ? batch.getTrainer().getLastName()
                                    : ""
                    );
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
                batch.getOrganization().getId(),
                batch.getCourse().getId(),
                batch.getCourse().getName(),
                batch.getTrainer() != null
                        ? batch.getTrainer().getId()
                        : null,
                trainerName != null
                        ? trainerName.trim()
                        : null
        );
    }


    // =========================================================
    // ATTENDANCE RESPONSE
    // =========================================================

    private AttendanceResponse toAttendanceResponse(
            Attendance attendance
    ) {

        String studentName =
                attendance.getStudent()
                        .getFirstName()
                        + " "
                        + (
                        attendance.getStudent()
                                .getLastName() != null
                                ? attendance.getStudent()
                                        .getLastName()
                                : ""
                );

        return new AttendanceResponse(
                attendance.getId(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getRemarks(),
                attendance.getOrganization().getId(),
                attendance.getBatch().getId(),
                attendance.getBatch().getName(),
                attendance.getBatch().getCode(),
                attendance.getStudent().getId(),
                studentName.trim(),
                attendance.getStudent().getEmail()
        );
    }
}