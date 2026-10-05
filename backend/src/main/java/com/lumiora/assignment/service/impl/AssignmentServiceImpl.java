package com.lumiora.assignment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumiora.assignment.dto.AssignmentCreateRequest;
import com.lumiora.assignment.dto.AssignmentResponse;
import com.lumiora.assignment.dto.AssignmentSubmissionResponse;
import com.lumiora.assignment.dto.AssignmentUpdateRequest;
import com.lumiora.assignment.dto.SubmissionCreateRequest;
import com.lumiora.assignment.dto.SubmissionGradeRequest;
import com.lumiora.assignment.dto.SubmissionUpdateRequest;
import com.lumiora.assignment.entity.Assignment;
import com.lumiora.assignment.entity.AssignmentStatus;
import com.lumiora.assignment.entity.AssignmentSubmission;
import com.lumiora.assignment.entity.SubmissionStatus;
import com.lumiora.assignment.repository.AssignmentRepository;
import com.lumiora.assignment.repository.AssignmentSubmissionRepository;

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.entity.BatchStatus;
import com.lumiora.batch.repository.BatchRepository;

import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.repository.EnrollmentRepository;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;
import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl
        implements com.lumiora.assignment.service.AssignmentService {

    private final AssignmentRepository assignmentRepository;

    private final AssignmentSubmissionRepository submissionRepository;

    private final BatchRepository batchRepository;

    private final EnrollmentRepository enrollmentRepository;

    private final UserRepository userRepository;

    // =========================================================
    // CREATE ASSIGNMENT
    // =========================================================

    @Override
    public AssignmentResponse createAssignment(
            AssignmentCreateRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        String role = getRole(currentUser);

        if (!isAssignmentManager(role)) {
            throw new IllegalArgumentException(
                "Only SUPER_ADMIN, ADMIN and TRAINER can create assignments"
            );
        }

        Long organizationId = validateOrganizationAccess(
            currentUser,
            request.getOrganizationId()
        );

        Batch batch = batchRepository
            .findByIdAndOrganization_Id(
                request.getBatchId(),
                organizationId
            )
            .orElseThrow(() -> new IllegalArgumentException(
                "Batch not found in this organization"
            ));

        validateBatch(batch);
        validateAssignmentManagerForBatch(currentUser, batch);

        Assignment assignment = new Assignment();

        assignment.setTitle(request.getTitle().trim());
        assignment.setDescription(normalize(request.getDescription()));
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxMarks(
            request.getMaxMarks().setScale(2, RoundingMode.HALF_UP)
        );
        assignment.setResourceUrl(normalize(request.getResourceUrl()));
        assignment.setStatus(AssignmentStatus.OPEN);
        assignment.setActive(true);
        assignment.setOrganization(batch.getOrganization());
        assignment.setBatch(batch);
        assignment.setCreatedBy(currentUser);

        Assignment saved = assignmentRepository.save(assignment);

        return toAssignmentResponse(
            assignmentRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalStateException(
                    "Assignment not found after creation"
                ))
        );
    }

    // =========================================================
    // GET ALL ASSIGNMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        String role = getRole(currentUser);

        List<Assignment> assignments;

        if ("SUPER_ADMIN".equals(role)) {
            assignments = assignmentRepository.findAll();

        } else if ("ADMIN".equals(role)) {
            assignments = assignmentRepository
                .findAllByOrganization_Id(getOrganizationId(currentUser));

        } else if ("TRAINER".equals(role)) {
            assignments = assignmentRepository
                .findAllByOrganization_IdAndBatch_Trainer_Id(
                    getOrganizationId(currentUser),
                    currentUser.getId()
                );

        } else if ("STUDENT".equals(role)) {
            assignments = findStudentAssignments(currentUser);

        } else {
            throw new IllegalArgumentException(
                "You are not authorized to view assignments"
            );
        }

        return assignments.stream()
            .filter(Assignment::isActive)
            .sorted(Comparator
                .comparing(Assignment::getDueDate)
                .thenComparing(Assignment::getTitle))
            .map(this::toAssignmentResponse)
            .toList();
    }

    // =========================================================
    // MY ASSIGNMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getMyAssignments(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        String role = getRole(currentUser);

        List<Assignment> assignments;

        if ("STUDENT".equals(role)) {
            assignments = findStudentAssignments(currentUser);

        } else if ("TRAINER".equals(role)) {
            assignments = assignmentRepository
                .findAllByOrganization_IdAndBatch_Trainer_Id(
                    getOrganizationId(currentUser),
                    currentUser.getId()
                );

        } else {
            throw new IllegalArgumentException(
                "My assignments are available only to STUDENT and TRAINER"
            );
        }

        return assignments.stream()
            .filter(Assignment::isActive)
            .sorted(Comparator.comparing(Assignment::getDueDate))
            .map(this::toAssignmentResponse)
            .toList();
    }

    // =========================================================
    // GET ASSIGNMENT BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(
            Long assignmentId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Assignment assignment = findActiveAssignment(assignmentId);

        validateAssignmentViewAccess(assignment, currentUser);

        return toAssignmentResponse(assignment);
    }

    // =========================================================
    // ASSIGNMENTS BY BATCH
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByBatch(
            Long batchId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Batch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Batch not found with id: " + batchId
            ));

        validateAssignmentManagerOrStudentAccess(currentUser, batch);

        return assignmentRepository
            .findAllByOrganization_IdAndBatch_Id(
                batch.getOrganization().getId(),
                batchId
            )
            .stream()
            .filter(Assignment::isActive)
            .sorted(Comparator.comparing(Assignment::getDueDate))
            .map(this::toAssignmentResponse)
            .toList();
    }

    // =========================================================
    // UPDATE ASSIGNMENT
    // =========================================================

    @Override
    public AssignmentResponse updateAssignment(
            Long assignmentId,
            AssignmentUpdateRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Assignment assignment = findActiveAssignment(assignmentId);

        validateAssignmentManagerAccess(assignment, currentUser);

        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new IllegalArgumentException(
                    "Assignment title cannot be blank"
                );
            }
            assignment.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            assignment.setDescription(normalize(request.getDescription()));
        }

        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate());
        }

        if (request.getMaxMarks() != null) {
            if (request.getMaxMarks().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                    "Maximum marks must be greater than zero"
                );
            }

            assignment.setMaxMarks(
                request.getMaxMarks().setScale(2, RoundingMode.HALF_UP)
            );
        }

        if (request.getResourceUrl() != null) {
            assignment.setResourceUrl(normalize(request.getResourceUrl()));
        }

        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
        }

        if (request.getActive() != null) {
            assignment.setActive(request.getActive());
        }

        return toAssignmentResponse(assignmentRepository.save(assignment));
    }

    // =========================================================
    // DEACTIVATE ASSIGNMENT
    // =========================================================

    @Override
    public void deactivateAssignment(
            Long assignmentId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Assignment assignment = findActiveAssignment(assignmentId);

        validateAssignmentManagerAccess(assignment, currentUser);

        assignment.setActive(false);
        assignmentRepository.save(assignment);
    }

    // =========================================================
    // CREATE STUDENT SUBMISSION
    // =========================================================

    @Override
    public AssignmentSubmissionResponse createSubmission(
            Long assignmentId,
            SubmissionCreateRequest request,
            Authentication authentication) {

        User student = getCurrentUser(authentication);

        if (!"STUDENT".equals(getRole(student))) {
            throw new IllegalArgumentException(
                "Only students can submit assignments"
            );
        }

        validateStudentActive(student);
        validateSubmissionContent(
            request.getSubmissionText(),
            request.getFileUrl()
        );

        Assignment assignment = findActiveAssignment(assignmentId);

        requireOrganization(
            student,
            assignment.getOrganization().getId()
        );

        if (assignment.getStatus() != AssignmentStatus.OPEN) {
            throw new IllegalArgumentException(
                "This assignment is closed"
            );
        }

        if (!isStudentEnrolled(student, assignment.getBatch().getId())) {
            throw new IllegalArgumentException(
                "You are not actively enrolled in this assignment's batch"
            );
        }

        boolean alreadySubmitted = submissionRepository
            .findByAssignment_IdAndStudent_IdAndActiveTrue(
                assignmentId,
                student.getId()
            )
            .isPresent();

        if (alreadySubmitted) {
            throw new IllegalArgumentException(
                "You have already submitted this assignment. Use the resubmit endpoint."
            );
        }

        LocalDateTime submittedAt = LocalDateTime.now();

        AssignmentSubmission submission = new AssignmentSubmission();

        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setOrganization(assignment.getOrganization());
        submission.setSubmissionText(normalize(request.getSubmissionText()));
        submission.setFileUrl(normalize(request.getFileUrl()));
        submission.setSubmittedAt(submittedAt);
        submission.setStatus(
            submittedAt.isAfter(assignment.getDueDate())
                ? SubmissionStatus.LATE
                : SubmissionStatus.SUBMITTED
        );
        submission.setActive(true);

        return toSubmissionResponse(
            submissionRepository.save(submission)
        );
    }

    // =========================================================
    // MY SUBMISSIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponse> getMySubmissions(
            Authentication authentication) {

        User student = getCurrentUser(authentication);

        if (!"STUDENT".equals(getRole(student))) {
            throw new IllegalArgumentException(
                "This endpoint is available only to students"
            );
        }

        Long organizationId = getOrganizationId(student);

        return submissionRepository
            .findAllByOrganization_IdAndStudent_IdAndActiveTrueOrderBySubmittedAtDesc(
                organizationId,
                student.getId()
            )
            .stream()
            .map(this::toSubmissionResponse)
            .toList();
    }

    // =========================================================
    // SUBMISSIONS FOR ONE ASSIGNMENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponse> getSubmissionsForAssignment(
            Long assignmentId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Assignment assignment = findActiveAssignment(assignmentId);

        validateAssignmentManagerAccess(assignment, currentUser);

        List<AssignmentSubmission> submissions;

        if ("SUPER_ADMIN".equals(getRole(currentUser))) {
            submissions = submissionRepository
                .findAllByAssignment_IdAndActiveTrueOrderBySubmittedAtDesc(
                    assignmentId
                );
        } else {
            submissions = submissionRepository
                .findAllByAssignment_IdAndOrganization_IdAndActiveTrueOrderBySubmittedAtDesc(
                    assignmentId,
                    getOrganizationId(currentUser)
                );
        }

        return submissions.stream()
            .map(this::toSubmissionResponse)
            .toList();
    }

    // =========================================================
    // RESUBMIT ASSIGNMENT
    // =========================================================

    @Override
    public AssignmentSubmissionResponse resubmit(
            Long submissionId,
            SubmissionUpdateRequest request,
            Authentication authentication) {

        User student = getCurrentUser(authentication);

        if (!"STUDENT".equals(getRole(student))) {
            throw new IllegalArgumentException(
                "Only students can resubmit assignments"
            );
        }

        validateStudentActive(student);
        validateSubmissionContent(
            request.getSubmissionText(),
            request.getFileUrl()
        );

        AssignmentSubmission submission = submissionRepository
            .findByIdAndActiveTrue(submissionId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Submission not found with id: " + submissionId
            ));

        if (!submission.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException(
                "You can only resubmit your own assignments"
            );
        }

        Assignment assignment = submission.getAssignment();

        requireOrganization(
            student,
            assignment.getOrganization().getId()
        );

        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new IllegalArgumentException(
                "A graded assignment cannot be resubmitted"
            );
        }

        if (!assignment.isActive()
                || assignment.getStatus() != AssignmentStatus.OPEN) {
            throw new IllegalArgumentException(
                "This assignment is closed"
            );
        }

        if (!isStudentEnrolled(student, assignment.getBatch().getId())) {
            throw new IllegalArgumentException(
                "You are no longer actively enrolled in this batch"
            );
        }

        LocalDateTime submittedAt = LocalDateTime.now();

        submission.setSubmissionText(normalize(request.getSubmissionText()));
        submission.setFileUrl(normalize(request.getFileUrl()));
        submission.setSubmittedAt(submittedAt);
        submission.setStatus(
            submittedAt.isAfter(assignment.getDueDate())
                ? SubmissionStatus.LATE
                : SubmissionStatus.SUBMITTED
        );

        return toSubmissionResponse(
            submissionRepository.save(submission)
        );
    }

    // =========================================================
    // GRADE SUBMISSION
    // =========================================================

    @Override
    public AssignmentSubmissionResponse gradeSubmission(
            Long submissionId,
            SubmissionGradeRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        AssignmentSubmission submission = submissionRepository
            .findByIdAndActiveTrue(submissionId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Submission not found with id: " + submissionId
            ));

        Assignment assignment = submission.getAssignment();

        validateAssignmentManagerAccess(assignment, currentUser);

        BigDecimal marks = request.getMarksAwarded();

        if (marks.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Marks cannot be negative"
            );
        }

        if (marks.compareTo(assignment.getMaxMarks()) > 0) {
            throw new IllegalArgumentException(
                "Marks cannot exceed the assignment's maximum marks"
            );
        }

        submission.setMarksAwarded(
            marks.setScale(2, RoundingMode.HALF_UP)
        );
        submission.setFeedback(normalize(request.getFeedback()));
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(currentUser);

        return toSubmissionResponse(
            submissionRepository.save(submission)
        );
    }

    // =========================================================
    // ACCESS AND VALIDATION HELPERS
    // =========================================================

    private User getCurrentUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException(
                "Authenticated user not found"
            ));
    }

    private String getRole(User user) {
        return user.getRole().getName();
    }

    private boolean isAssignmentManager(String role) {
        return "SUPER_ADMIN".equals(role)
            || "ADMIN".equals(role)
            || "TRAINER".equals(role);
    }

    private Long getOrganizationId(User user) {

        if (user.getOrganization() == null) {
            throw new IllegalArgumentException(
                "User is not assigned to an organization"
            );
        }

        return user.getOrganization().getId();
    }

    private Long validateOrganizationAccess(
            User currentUser,
            Long requestedOrganizationId) {

        if ("SUPER_ADMIN".equals(getRole(currentUser))) {
            return requestedOrganizationId;
        }

        Long currentOrganizationId = getOrganizationId(currentUser);

        if (!currentOrganizationId.equals(requestedOrganizationId)) {
            throw new IllegalArgumentException(
                "You cannot manage assignments for another organization"
            );
        }

        return currentOrganizationId;
    }

    private void requireOrganization(User user, Long organizationId) {

        if (!getOrganizationId(user).equals(organizationId)) {
            throw new IllegalArgumentException(
                "You cannot access another organization's data"
            );
        }
    }

    private void validateBatch(Batch batch) {

        if (!batch.isActive()) {
            throw new IllegalArgumentException("Batch is inactive");
        }

        if (batch.getStatus() == BatchStatus.CANCELLED
                || batch.getStatus() == BatchStatus.COMPLETED) {
            throw new IllegalArgumentException(
                "Cannot create assignments for a completed or cancelled batch"
            );
        }
    }

    private void validateAssignmentManagerForBatch(
            User currentUser,
            Batch batch) {

        String role = getRole(currentUser);

        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        requireOrganization(
            currentUser,
            batch.getOrganization().getId()
        );

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("TRAINER".equals(role)) {
            if (batch.getTrainer() == null
                    || !batch.getTrainer().getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException(
                    "You can manage assignments only for your assigned batches"
                );
            }

            return;
        }

        throw new IllegalArgumentException(
            "You are not authorized to manage assignments"
        );
    }

    private void validateAssignmentManagerAccess(
            Assignment assignment,
            User currentUser) {

        if (!isAssignmentManager(getRole(currentUser))) {
            throw new IllegalArgumentException(
                "You are not authorized to manage assignments"
            );
        }

        validateAssignmentManagerForBatch(
            currentUser,
            assignment.getBatch()
        );
    }

    private void validateAssignmentManagerOrStudentAccess(
            User currentUser,
            Batch batch) {

        String role = getRole(currentUser);

        if (isAssignmentManager(role)) {
            validateAssignmentManagerForBatch(currentUser, batch);
            return;
        }

        if ("STUDENT".equals(role)) {
            requireOrganization(
                currentUser,
                batch.getOrganization().getId()
            );

            if (!isStudentEnrolled(currentUser, batch.getId())) {
                throw new IllegalArgumentException(
                    "You are not enrolled in this batch"
                );
            }

            return;
        }

        throw new IllegalArgumentException(
            "You are not authorized to view assignments"
        );
    }

    private void validateAssignmentViewAccess(
            Assignment assignment,
            User currentUser) {

        String role = getRole(currentUser);

        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        requireOrganization(
            currentUser,
            assignment.getOrganization().getId()
        );

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("TRAINER".equals(role)) {
            validateAssignmentManagerForBatch(
                currentUser,
                assignment.getBatch()
            );
            return;
        }

        if ("STUDENT".equals(role)) {
            if (!isStudentEnrolled(
                    currentUser,
                    assignment.getBatch().getId())) {
                throw new IllegalArgumentException(
                    "You are not enrolled in this assignment's batch"
                );
            }
            return;
        }

        throw new IllegalArgumentException(
            "You are not authorized to view this assignment"
        );
    }

    private void validateStudentActive(User student) {

        if (student.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException(
                "Student account is inactive"
            );
        }
    }

    private boolean isStudentEnrolled(User student, Long batchId) {

        if (student.getOrganization() == null) {
            return false;
        }

        List<Enrollment> enrollments = enrollmentRepository
            .findAllByOrganization_IdAndStudent_Id(
                student.getOrganization().getId(),
                student.getId()
            );

        return enrollments.stream().anyMatch(enrollment ->
            enrollment.getStatus() == EnrollmentStatus.ENROLLED
                && enrollment.getBatch() != null
                && enrollment.getBatch().getId().equals(batchId)
        );
    }

    private List<Assignment> findStudentAssignments(User student) {

        Long organizationId = getOrganizationId(student);

        List<Enrollment> enrollments = enrollmentRepository
            .findAllByOrganization_IdAndStudent_Id(
                organizationId,
                student.getId()
            );

        Set<Long> batchIds = new LinkedHashSet<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ENROLLED
                    && enrollment.getBatch() != null) {
                batchIds.add(enrollment.getBatch().getId());
            }
        }

        if (batchIds.isEmpty()) {
            return List.of();
        }

        return assignmentRepository
            .findAllByOrganization_IdAndBatch_IdIn(
                organizationId,
                new ArrayList<>(batchIds)
            );
    }

    private Assignment findActiveAssignment(Long assignmentId) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Assignment not found with id: " + assignmentId
            ));

        if (!assignment.isActive()) {
            throw new IllegalArgumentException(
                "Assignment is inactive"
            );
        }

        return assignment;
    }

    private void validateSubmissionContent(
            String submissionText,
            String fileUrl) {

        boolean hasText = submissionText != null
            && !submissionText.isBlank();

        boolean hasFile = fileUrl != null
            && !fileUrl.isBlank();

        if (!hasText && !hasFile) {
            throw new IllegalArgumentException(
                "Provide submission text or a file URL"
            );
        }
    }

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    // =========================================================
    // RESPONSE MAPPERS
    // =========================================================

    private AssignmentResponse toAssignmentResponse(
            Assignment assignment) {

        Batch batch = assignment.getBatch();

        User trainer = batch.getTrainer();
        User createdBy = assignment.getCreatedBy();

        String trainerName = trainer == null
            ? null
            : fullName(trainer);

        return new AssignmentResponse(
            assignment.getId(),
            assignment.getTitle(),
            assignment.getDescription(),
            assignment.getDueDate(),
            assignment.getMaxMarks(),
            assignment.getResourceUrl(),
            assignment.getStatus(),
            assignment.isActive(),
            assignment.getOrganization().getId(),
            batch.getId(),
            batch.getName(),
            batch.getCourse() == null ? null : batch.getCourse().getName(),
            trainer == null ? null : trainer.getId(),
            trainerName,
            createdBy.getId(),
            fullName(createdBy)
        );
    }

    private AssignmentSubmissionResponse toSubmissionResponse(
            AssignmentSubmission submission) {

        User student = submission.getStudent();
        User gradedBy = submission.getGradedBy();

        return new AssignmentSubmissionResponse(
            submission.getId(),
            submission.getAssignment().getId(),
            submission.getAssignment().getTitle(),
            student.getId(),
            fullName(student),
            student.getEmail(),
            submission.getSubmissionText(),
            submission.getFileUrl(),
            submission.getSubmittedAt(),
            submission.getStatus(),
            submission.getMarksAwarded(),
            submission.getFeedback(),
            submission.getGradedAt(),
            gradedBy == null ? null : gradedBy.getId(),
            gradedBy == null ? null : fullName(gradedBy)
        );
    }

    private String fullName(User user) {

        String firstName = user.getFirstName() == null
            ? ""
            : user.getFirstName().trim();

        String lastName = user.getLastName() == null
            ? ""
            : user.getLastName().trim();

        return (firstName + " " + lastName).trim();
    }
}