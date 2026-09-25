package com.lumiora.timetable.service.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.entity.BatchStatus;
import com.lumiora.batch.repository.BatchRepository;

import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.repository.EnrollmentRepository;

import com.lumiora.entity.auth.User;

import com.lumiora.repository.UserRepository;

import com.lumiora.timetable.dto.TimetableCreateRequest;
import com.lumiora.timetable.dto.TimetableResponse;
import com.lumiora.timetable.dto.TimetableUpdateRequest;
import com.lumiora.timetable.entity.TimetableEntry;
import com.lumiora.timetable.repository.TimetableRepository;
import com.lumiora.timetable.service.TimetableService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TimetableServiceImpl
        implements TimetableService {


    private final TimetableRepository timetableRepository;

    private final BatchRepository batchRepository;

    private final EnrollmentRepository enrollmentRepository;

    private final UserRepository userRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public TimetableResponse create(
            TimetableCreateRequest request,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        Long organizationId =
                validateOrganizationAccess(
                        currentUser,
                        request.getOrganizationId()
                );


        Batch batch =
                batchRepository
                        .findByIdAndOrganization_Id(
                                request.getBatchId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found in this organization: "
                                                + request.getBatchId()
                                )
                        );


        validateBatch(batch);

        validateTime(
                request.getStartTime(),
                request.getEndTime()
        );


        checkConflicts(
                null,
                organizationId,
                batch,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRoom()
        );


        TimetableEntry entry =
                new TimetableEntry();

        entry.setDayOfWeek(
                request.getDayOfWeek()
        );

        entry.setStartTime(
                request.getStartTime()
        );

        entry.setEndTime(
                request.getEndTime()
        );

        entry.setRoom(
                normalize(request.getRoom())
        );

        entry.setTopic(
                normalize(request.getTopic())
        );

        entry.setNotes(
                normalize(request.getNotes())
        );

        entry.setActive(true);

        entry.setOrganization(
                batch.getOrganization()
        );

        entry.setBatch(
                batch
        );


        TimetableEntry saved =
                timetableRepository.save(entry);


        TimetableEntry refreshed =
                timetableRepository
                        .findById(saved.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Timetable entry not found after creation"
                                )
                        );


        return toResponse(refreshed);
    }


    // =========================================================
    // FIND ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> findAll(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();

        List<TimetableEntry> entries;


        if ("SUPER_ADMIN".equals(role)) {

            entries =
                    timetableRepository.findAll();

        } else if ("ADMIN".equals(role)) {

            Long organizationId =
                    getOrganizationId(currentUser);

            entries =
                    timetableRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

        } else if ("TRAINER".equals(role)) {

            Long organizationId =
                    getOrganizationId(currentUser);

            entries =
                    timetableRepository
                            .findAllByOrganization_IdAndBatch_Trainer_IdAndActiveTrue(
                                    organizationId,
                                    currentUser.getId()
                            );

        } else if ("STUDENT".equals(role)) {

            entries =
                    findStudentTimetable(
                            currentUser
                    );

        } else {

            throw new IllegalArgumentException(
                    "You are not authorized to access the timetable"
            );
        }


        return mapAndSort(entries);
    }


    // =========================================================
    // FIND BY BATCH
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> findByBatch(
            Long batchId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        Batch batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found with id: "
                                                + batchId
                                )
                        );


        Long organizationId =
                batch.getOrganization() != null
                        ? batch.getOrganization().getId()
                        : null;


        if (organizationId == null) {

            throw new IllegalArgumentException(
                    "Batch is not assigned to an organization"
            );
        }


        List<TimetableEntry> entries;


        if ("SUPER_ADMIN".equals(role)) {

            entries =
                    timetableRepository
                            .findAllByOrganization_IdAndBatch_Id(
                                    organizationId,
                                    batchId
                            );

        } else if ("ADMIN".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );

            entries =
                    timetableRepository
                            .findAllByOrganization_IdAndBatch_Id(
                                    organizationId,
                                    batchId
                            );

        } else if ("TRAINER".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );

            if (batch.getTrainer() == null
                    || !batch.getTrainer()
                    .getId()
                    .equals(currentUser.getId())) {

                throw new IllegalArgumentException(
                        "You are not the trainer assigned to this batch"
                );
            }

            entries =
                    timetableRepository
                            .findAllByOrganization_IdAndBatch_Id(
                                    organizationId,
                                    batchId
                            )
                            .stream()
                            .filter(
                                    TimetableEntry::isActive
                            )
                            .toList();

        } else if ("STUDENT".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );

            boolean enrolled =
                    isStudentEnrolled(
                            currentUser,
                            batchId
                    );

            if (!enrolled) {

                throw new IllegalArgumentException(
                        "You are not enrolled in this batch"
                );
            }

            entries =
                    timetableRepository
                            .findAllByOrganization_IdAndBatch_Id(
                                    organizationId,
                                    batchId
                            )
                            .stream()
                            .filter(
                                    TimetableEntry::isActive
                            )
                            .toList();

        } else {

            throw new IllegalArgumentException(
                    "You are not authorized to access this timetable"
            );
        }


        return mapAndSort(entries);
    }


    // =========================================================
    // FIND BY TRAINER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> findByTrainer(
            Long trainerId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        if ("TRAINER".equals(role)
                && !currentUser.getId().equals(trainerId)) {

            throw new IllegalArgumentException(
                    "Trainers can access only their own timetable"
            );
        }


        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)
                && !"TRAINER".equals(role)) {

            throw new IllegalArgumentException(
                    "You are not authorized to access trainer timetables"
            );
        }


        User trainer =
                userRepository
                        .findById(trainerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Trainer not found with id: "
                                                + trainerId
                                )
                        );


        if (trainer.getRole() == null
                || !"TRAINER".equals(
                trainer.getRole().getName()
        )) {

            throw new IllegalArgumentException(
                    "Selected user is not a TRAINER"
            );
        }


        if (trainer.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "Trainer is not assigned to an organization"
            );
        }


        Long trainerOrganizationId =
                trainer.getOrganization().getId();


        if ("ADMIN".equals(role)
                || "TRAINER".equals(role)) {

            requireOrganization(
                    currentUser,
                    trainerOrganizationId
            );
        }


        List<TimetableEntry> entries =
                timetableRepository
                        .findAllByOrganization_IdAndBatch_Trainer_IdAndActiveTrue(
                                trainerOrganizationId,
                                trainerId
                        );


        return mapAndSort(entries);
    }


    // =========================================================
    // MY TIMETABLE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TimetableResponse> findMyTimetable(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        if ("TRAINER".equals(role)) {

            Long organizationId =
                    getOrganizationId(currentUser);

            return mapAndSort(
                    timetableRepository
                            .findAllByOrganization_IdAndBatch_Trainer_IdAndActiveTrue(
                                    organizationId,
                                    currentUser.getId()
                            )
            );
        }


        if ("STUDENT".equals(role)) {

            return mapAndSort(
                    findStudentTimetable(
                            currentUser
                    )
            );
        }


        throw new IllegalArgumentException(
                "My timetable is available only for STUDENT and TRAINER"
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public TimetableResponse update(
            Long id,
            TimetableUpdateRequest request,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        TimetableEntry entry =
                findAccessibleEntry(
                        id,
                        currentUser
                );


        Long organizationId =
                entry.getOrganization().getId();


        Batch batch =
                batchRepository
                        .findByIdAndOrganization_Id(
                                request.getBatchId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found in this organization: "
                                                + request.getBatchId()
                                )
                        );


        validateBatch(batch);

        validateTime(
                request.getStartTime(),
                request.getEndTime()
        );


        checkConflicts(
                entry.getId(),
                organizationId,
                batch,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRoom()
        );


        entry.setBatch(batch);

        entry.setDayOfWeek(
                request.getDayOfWeek()
        );

        entry.setStartTime(
                request.getStartTime()
        );

        entry.setEndTime(
                request.getEndTime()
        );

        entry.setRoom(
                normalize(request.getRoom())
        );

        entry.setTopic(
                normalize(request.getTopic())
        );

        entry.setNotes(
                normalize(request.getNotes())
        );


        if (request.getActive() != null) {

            entry.setActive(
                    request.getActive()
            );
        }


        TimetableEntry updated =
                timetableRepository.save(entry);


        TimetableEntry refreshed =
                timetableRepository
                        .findById(updated.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Timetable entry not found after update"
                                )
                        );


        return toResponse(refreshed);
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            Long id,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        TimetableEntry entry =
                findAccessibleEntry(
                        id,
                        currentUser
                );


        entry.setActive(false);

        timetableRepository.save(entry);
    }


    // =========================================================
    // STUDENT TIMETABLE
    // =========================================================

    private List<TimetableEntry> findStudentTimetable(
            User student
    ) {

        Long organizationId =
                getOrganizationId(student);


        List<Enrollment> enrollments =
                enrollmentRepository
                        .findAllByOrganization_IdAndStudent_Id(
                                organizationId,
                                student.getId()
                        );


        Set<Long> activeBatchIds =
                new HashSet<>();


        for (Enrollment enrollment : enrollments) {

            if (enrollment.getStatus()
                    == EnrollmentStatus.ENROLLED
                    && enrollment.getBatch() != null) {

                activeBatchIds.add(
                        enrollment.getBatch().getId()
                );
            }
        }


        if (activeBatchIds.isEmpty()) {

            return new ArrayList<>();
        }


        return timetableRepository
                .findAllByOrganization_IdAndBatch_IdInAndActiveTrue(
                        organizationId,
                        activeBatchIds
                );
    }


    // =========================================================
    // ACCESSIBLE ENTRY
    // =========================================================

    private TimetableEntry findAccessibleEntry(
            Long id,
            User currentUser
    ) {

        String role =
                currentUser.getRole().getName();


        if ("SUPER_ADMIN".equals(role)) {

            return timetableRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Timetable entry not found with id: "
                                            + id
                            )
                    );
        }


        Long organizationId =
                getOrganizationId(currentUser);


        return timetableRepository
                .findByIdAndOrganization_Id(
                        id,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Timetable entry not found"
                        )
                );
    }


    // =========================================================
    // VALIDATE ADMIN ACCESS
    // =========================================================

    private void validateAdminAccess(
            User user
    ) {

        String role =
                user.getRole().getName();


        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN and ADMIN can manage timetables"
            );
        }
    }


    // =========================================================
    // ORGANIZATION ACCESS
    // =========================================================

    private Long validateOrganizationAccess(
            User currentUser,
            Long requestedOrganizationId
    ) {

        if ("SUPER_ADMIN".equals(
                currentUser.getRole().getName())) {

            return requestedOrganizationId;
        }


        Long currentOrganizationId =
                getOrganizationId(currentUser);


        if (!currentOrganizationId.equals(
                requestedOrganizationId
        )) {

            throw new IllegalArgumentException(
                    "You cannot manage timetable for another organization"
            );
        }


        return currentOrganizationId;
    }


    private void requireOrganization(
            User user,
            Long organizationId
    ) {

        Long currentOrganizationId =
                getOrganizationId(user);


        if (!currentOrganizationId.equals(
                organizationId
        )) {

            throw new IllegalArgumentException(
                    "You cannot access another organization's timetable"
            );
        }
    }


    private Long getOrganizationId(
            User user
    ) {

        if (user.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "User is not assigned to an organization"
            );
        }


        return user.getOrganization().getId();
    }


    // =========================================================
    // BATCH VALIDATION
    // =========================================================

    private void validateBatch(
            Batch batch
    ) {

        if (!batch.isActive()) {

            throw new IllegalArgumentException(
                    "Batch is inactive"
            );
        }


        if (batch.getStatus()
                == BatchStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot create timetable for a cancelled batch"
            );
        }


        if (batch.getStatus()
                == BatchStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Cannot create timetable for a completed batch"
            );
        }
    }


    // =========================================================
    // TIME VALIDATION
    // =========================================================

    private void validateTime(
            LocalTime startTime,
            LocalTime endTime
    ) {

        if (startTime == null
                || endTime == null) {

            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }


        if (!endTime.isAfter(startTime)) {

            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }
    }


    // =========================================================
    // CONFLICT DETECTION
    // =========================================================

    private void checkConflicts(
            Long currentEntryId,
            Long organizationId,
            Batch batch,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String room
    ) {

        List<TimetableEntry> entries =
                timetableRepository
                        .findAllByOrganization_Id(
                                organizationId
                        );


        String normalizedRoom =
                normalize(room);


        for (TimetableEntry existing : entries) {

            if (!existing.isActive()) {
                continue;
            }


            if (currentEntryId != null
                    && currentEntryId.equals(
                    existing.getId())) {

                continue;
            }


            if (existing.getDayOfWeek()
                    != dayOfWeek) {

                continue;
            }


            if (!isOverlapping(
                    startTime,
                    endTime,
                    existing.getStartTime(),
                    existing.getEndTime()
            )) {

                continue;
            }


            /*
             * SAME BATCH
             */
            if (existing.getBatch() != null
                    && existing.getBatch()
                    .getId()
                    .equals(batch.getId())) {

                throw new IllegalArgumentException(
                        "Batch already has an overlapping timetable entry on "
                                + dayOfWeek
                                + " from "
                                + existing.getStartTime()
                                + " to "
                                + existing.getEndTime()
                );
            }


            /*
             * SAME TRAINER
             */
            if (batch.getTrainer() != null
                    && existing.getBatch() != null
                    && existing.getBatch()
                    .getTrainer() != null
                    && existing.getBatch()
                    .getTrainer()
                    .getId()
                    .equals(
                            batch.getTrainer().getId()
                    )) {

                throw new IllegalArgumentException(
                        "Trainer already has another class at this time"
                );
            }


            /*
             * SAME ROOM
             */
            if (normalizedRoom != null
                    && existing.getRoom() != null
                    && normalizedRoom.equalsIgnoreCase(
                    existing.getRoom().trim()
            )) {

                throw new IllegalArgumentException(
                        "Room "
                                + normalizedRoom
                                + " is already occupied at this time"
                );
            }
        }
    }


    private boolean isOverlapping(
            LocalTime start1,
            LocalTime end1,
            LocalTime start2,
            LocalTime end2
    ) {

        return start1.isBefore(end2)
                && end1.isAfter(start2);
    }


    // =========================================================
    // STUDENT ENROLLMENT CHECK
    // =========================================================

    private boolean isStudentEnrolled(
            User student,
            Long batchId
    ) {

        Long organizationId =
                getOrganizationId(student);


        return enrollmentRepository
                .findAllByOrganization_IdAndStudent_Id(
                        organizationId,
                        student.getId()
                )
                .stream()
                .anyMatch(enrollment ->
                        enrollment.getBatch() != null
                                && enrollment.getBatch()
                                .getId()
                                .equals(batchId)
                                && enrollment.getStatus()
                                == EnrollmentStatus.ENROLLED
                );
    }


    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private TimetableResponse toResponse(
            TimetableEntry entry
    ) {

        Batch batch =
                entry.getBatch();


        Long courseId = null;

        String courseName = null;


        if (batch != null
                && batch.getCourse() != null) {

            courseId =
                    batch.getCourse().getId();

            courseName =
                    batch.getCourse().getName();
        }


        Long trainerId = null;

        String trainerName = null;


        if (batch != null
                && batch.getTrainer() != null) {

            trainerId =
                    batch.getTrainer().getId();

            trainerName =
                    (
                            batch.getTrainer().getFirstName()
                                    + " "
                                    + (
                                    batch.getTrainer()
                                            .getLastName() != null
                                            ? batch.getTrainer()
                                            .getLastName()
                                            : ""
                            )
                    ).trim();
        }


        return new TimetableResponse(

                entry.getId(),

                entry.getDayOfWeek(),

                entry.getStartTime(),

                entry.getEndTime(),

                entry.getRoom(),

                entry.getTopic(),

                entry.getNotes(),

                entry.isActive(),

                entry.getOrganization() != null
                        ? entry.getOrganization().getId()
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

                batch != null
                        ? batch.getStatus()
                        : null,

                courseId,

                courseName,

                trainerId,

                trainerName
        );
    }


    private List<TimetableResponse> mapAndSort(
            List<TimetableEntry> entries
    ) {

        return entries.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        TimetableEntry::getDayOfWeek
                                )
                                .thenComparing(
                                        TimetableEntry::getStartTime
                                )
                )
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser(
            Authentication authentication
    ) {

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }


    private String normalize(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }
}