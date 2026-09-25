package com.lumiora.exam.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.exam.dto.ExamCreateRequest;
import com.lumiora.exam.dto.ExamResponse;
import com.lumiora.exam.dto.ExamResultCreateRequest;
import com.lumiora.exam.dto.ExamResultResponse;
import com.lumiora.exam.dto.ExamResultUpdateRequest;
import com.lumiora.exam.dto.ExamUpdateRequest;

import com.lumiora.exam.entity.Exam;
import com.lumiora.exam.entity.ExamResult;
import com.lumiora.exam.entity.ExamStatus;

import com.lumiora.exam.repository.ExamRepository;
import com.lumiora.exam.repository.ExamResultRepository;
import com.lumiora.exam.service.ExamService;

import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamServiceImpl
        implements ExamService {


    private final ExamRepository examRepository;

    private final ExamResultRepository examResultRepository;

    private final BatchRepository batchRepository;

    private final EnrollmentRepository enrollmentRepository;

    private final UserRepository userRepository;


    // =========================================================
    // CREATE EXAM
    // =========================================================

    @Override
    public ExamResponse createExam(
            ExamCreateRequest request,
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


        Exam exam =
                new Exam();

        exam.setName(
                request.getName().trim()
        );

        exam.setDescription(
                normalize(request.getDescription())
        );

        exam.setExamDate(
                request.getExamDate()
        );

        exam.setMaxMarks(
                request.getMaxMarks()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        exam.setStatus(
                ExamStatus.SCHEDULED
        );

        exam.setActive(true);

        exam.setOrganization(
                batch.getOrganization()
        );

        exam.setBatch(
                batch
        );


        Exam saved =
                examRepository.save(exam);


        return toExamResponse(
                examRepository
                        .findById(saved.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Exam not found after creation"
                                ))
        );
    }


    // =========================================================
    // GET ALL EXAMS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getAllExams(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        List<Exam> exams;


        if ("SUPER_ADMIN".equals(role)) {

            exams =
                    examRepository.findAll();

        } else if ("ADMIN".equals(role)) {

            exams =
                    examRepository
                            .findAllByOrganization_Id(
                                    getOrganizationId(
                                            currentUser
                                    )
                            );

        } else if ("TRAINER".equals(role)) {

            Long organizationId =
                    getOrganizationId(currentUser);

            exams =
                    examRepository
                            .findAllByOrganization_IdAndBatch_Trainer_Id(
                                    organizationId,
                                    currentUser.getId()
                            );

        } else if ("STUDENT".equals(role)) {

            exams =
                    findStudentExams(
                            currentUser
                    );

        } else {

            throw new IllegalArgumentException(
                    "You are not authorized to view exams"
            );
        }


        return exams.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        Exam::getExamDate
                                )
                                .thenComparing(
                                        Exam::getName
                                )
                )
                .map(this::toExamResponse)
                .toList();
    }


    // =========================================================
    // GET EXAM BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ExamResponse getExamById(
            Long examId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        Exam exam =
                examRepository
                        .findById(examId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Exam not found with id: "
                                                + examId
                                )
                        );


        validateExamViewAccess(
                exam,
                currentUser,
                role
        );


        return toExamResponse(exam);
    }


    // =========================================================
    // EXAMS BY BATCH
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getExamsByBatch(
            Long batchId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        Batch batch =
                batchRepository
                        .findById(batchId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Batch not found with id: "
                                                + batchId
                                )
                        );


        if (batch.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "Batch is not assigned to an organization"
            );
        }


        Long organizationId =
                batch.getOrganization().getId();


        if ("SUPER_ADMIN".equals(role)) {

            // allowed

        } else if ("ADMIN".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
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
                        "You are not assigned to this batch"
                );
            }

        } else if ("STUDENT".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );

            if (!isStudentEnrolled(
                    currentUser,
                    batchId
            )) {

                throw new IllegalArgumentException(
                        "You are not enrolled in this batch"
                );
            }

        } else {

            throw new IllegalArgumentException(
                    "You are not authorized to view exams"
            );
        }


        return examRepository
                .findAllByOrganization_IdAndBatch_Id(
                        organizationId,
                        batchId
                )
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        Exam::getExamDate
                                )
                                .thenComparing(
                                        Exam::getName
                                )
                )
                .map(this::toExamResponse)
                .toList();
    }


    // =========================================================
    // MY EXAMS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getMyExams(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        if ("STUDENT".equals(role)) {

            return findStudentExams(currentUser)
                    .stream()
                    .sorted(
                            Comparator
                                    .comparing(
                                            Exam::getExamDate
                                    )
                    )
                    .map(
                            this::toExamResponse
                    )
                    .toList();
        }


        if ("TRAINER".equals(role)) {

            Long organizationId =
                    getOrganizationId(currentUser);

            return examRepository
                    .findAllByOrganization_IdAndBatch_Trainer_Id(
                            organizationId,
                            currentUser.getId()
                    )
                    .stream()
                    .sorted(
                            Comparator
                                    .comparing(
                                            Exam::getExamDate
                                    )
                    )
                    .map(
                            this::toExamResponse
                    )
                    .toList();
        }


        throw new IllegalArgumentException(
                "My exams are available only for STUDENT and TRAINER"
        );
    }


    // =========================================================
    // UPDATE EXAM
    // =========================================================

    @Override
    public ExamResponse updateExam(
            Long examId,
            ExamUpdateRequest request,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        Exam exam =
                findAccessibleExam(
                        examId,
                        currentUser
                );


        Long organizationId =
                exam.getOrganization().getId();


        if (request.getName() != null
                && !request.getName().isBlank()) {

            exam.setName(
                    request.getName().trim()
            );
        }


        if (request.getDescription() != null) {

            exam.setDescription(
                    normalize(
                            request.getDescription()
                    )
            );
        }


        if (request.getExamDate() != null) {

            exam.setExamDate(
                    request.getExamDate()
            );
        }


        if (request.getMaxMarks() != null) {

            if (request.getMaxMarks()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "Maximum marks must be greater than 0"
                );
            }

            exam.setMaxMarks(
                    request.getMaxMarks()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );
        }


        if (request.getBatchId() != null) {

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

            exam.setBatch(batch);
        }


        if (request.getStatus() != null) {

            exam.setStatus(
                    request.getStatus()
            );
        }


        if (request.getActive() != null) {

            exam.setActive(
                    request.getActive()
            );
        }


        Exam updated =
                examRepository.save(exam);


        return toExamResponse(
                examRepository
                        .findById(updated.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Exam not found after update"
                                ))
        );
    }


    // =========================================================
    // DEACTIVATE EXAM
    // =========================================================

    @Override
    public void deactivateExam(
            Long examId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        validateAdminAccess(currentUser);


        Exam exam =
                findAccessibleExam(
                        examId,
                        currentUser
                );


        exam.setActive(false);

        examRepository.save(exam);
    }


    // =========================================================
    // CREATE RESULT
    // =========================================================

    @Override
    public ExamResultResponse createResult(
            Long examId,
            ExamResultCreateRequest request,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)
                && !"TRAINER".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN, ADMIN and assigned TRAINER can create results"
            );
        }


        Exam exam =
                examRepository
                        .findById(examId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Exam not found with id: "
                                                + examId
                                )
                        );


        if (!exam.isActive()) {

            throw new IllegalArgumentException(
                    "Exam is inactive"
            );
        }


        if (exam.getStatus()
                == ExamStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot add result for a cancelled exam"
            );
        }


        Long organizationId =
                exam.getOrganization().getId();


        if ("ADMIN".equals(role)
                || "TRAINER".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );
        }


        if ("TRAINER".equals(role)) {

            if (exam.getBatch().getTrainer() == null
                    || !exam.getBatch()
                    .getTrainer()
                    .getId()
                    .equals(
                            currentUser.getId()
                    )) {

                throw new IllegalArgumentException(
                        "You can manage results only for your assigned batches"
                );
            }
        }


        User student =
                userRepository
                        .findById(
                                request.getStudentId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found with id: "
                                                + request.getStudentId()
                                )
                        );


        validateStudent(
                student,
                organizationId
        );


        if (!isStudentEnrolled(
                student,
                exam.getBatch().getId()
        )) {

            throw new IllegalArgumentException(
                    "Student is not enrolled in this batch"
            );
        }


        if (examResultRepository
                .findByExam_IdAndStudent_Id(
                        examId,
                        student.getId()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Result already exists for this student and exam"
            );
        }


        validateMarks(
                request.getMarks(),
                exam.getMaxMarks()
        );


        ExamResult result =
                new ExamResult();

        result.setExam(exam);

        result.setStudent(student);

        result.setOrganization(
                exam.getOrganization()
        );

        result.setMarks(
                request.getMarks()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        calculateResultValues(result);

        result.setRemarks(
                normalize(
                        request.getRemarks()
                )
        );

        result.setActive(true);


        ExamResult saved =
                examResultRepository.save(
                        result
                );


        return toResultResponse(
                examResultRepository
                        .findById(saved.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Result not found after creation"
                                ))
        );
    }


    // =========================================================
    // GET EXAM RESULTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultResponse> getExamResults(
            Long examId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        Exam exam =
                examRepository
                        .findById(examId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Exam not found with id: "
                                                + examId
                                )
                        );


        String role =
                currentUser.getRole().getName();


        Long organizationId =
                exam.getOrganization().getId();


        if ("SUPER_ADMIN".equals(role)) {

            // allowed

        } else if ("ADMIN".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );

        } else if ("TRAINER".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );

            if (exam.getBatch().getTrainer() == null
                    || !exam.getBatch()
                    .getTrainer()
                    .getId()
                    .equals(
                            currentUser.getId()
                    )) {

                throw new IllegalArgumentException(
                        "You are not assigned to this exam's batch"
                );
            }

        } else if ("STUDENT".equals(role)) {

            if (!isStudentEnrolled(
                    currentUser,
                    exam.getBatch().getId()
            )) {

                throw new IllegalArgumentException(
                        "You are not enrolled in this exam's batch"
                );
            }

            return examResultRepository
                    .findByExam_IdAndStudent_Id(
                            examId,
                            currentUser.getId()
                    )
                    .map(
                            result ->
                                    List.of(
                                            toResultResponse(
                                                    result
                                            )
                                    )
                    )
                    .orElseGet(
                            ArrayList::new
                    );

        } else {

            throw new IllegalArgumentException(
                    "You are not authorized to view exam results"
            );
        }


        return examResultRepository
                .findAllByExam_IdAndOrganization_Id(
                        examId,
                        organizationId
                )
                .stream()
                .map(this::toResultResponse)
                .toList();
    }


    // =========================================================
    // MY RESULTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultResponse> getMyResults(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);


        if (!"STUDENT".equals(
                currentUser.getRole().getName()
        )) {

            throw new IllegalArgumentException(
                    "My results are available only for students"
            );
        }


        Long organizationId =
                getOrganizationId(currentUser);


        return examResultRepository
                .findAllByStudent_IdAndOrganization_IdOrderByExam_ExamDateDesc(
                        currentUser.getId(),
                        organizationId
                )
                .stream()
                .filter(
                        ExamResult::isActive
                )
                .map(this::toResultResponse)
                .toList();
    }


    // =========================================================
    // STUDENT RESULTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultResponse> getStudentResults(
            Long studentId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        User student =
                userRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found with id: "
                                                + studentId
                                )
                        );


        validateStudentAccess(
                student,
                currentUser,
                role
        );


        Long organizationId =
                student.getOrganization()
                        .getId();


        return examResultRepository
                .findAllByStudent_IdAndOrganization_IdOrderByExam_ExamDateDesc(
                        studentId,
                        organizationId
                )
                .stream()
                .filter(
                        ExamResult::isActive
                )
                .map(this::toResultResponse)
                .toList();
    }


    // =========================================================
    // UPDATE RESULT
    // =========================================================

    @Override
    public ExamResultResponse updateResult(
            Long resultId,
            ExamResultUpdateRequest request,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)
                && !"TRAINER".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN, ADMIN and assigned TRAINER can update results"
            );
        }


        ExamResult result =
                examResultRepository
                        .findById(resultId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Result not found with id: "
                                                + resultId
                                )
                        );


        Exam exam =
                result.getExam();


        Long organizationId =
                exam.getOrganization().getId();


        if ("ADMIN".equals(role)
                || "TRAINER".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );
        }


        if ("TRAINER".equals(role)) {

            if (exam.getBatch().getTrainer() == null
                    || !exam.getBatch()
                    .getTrainer()
                    .getId()
                    .equals(
                            currentUser.getId()
                    )) {

                throw new IllegalArgumentException(
                        "You can update results only for your assigned batches"
                );
            }
        }


        if (request.getMarks() != null) {

            validateMarks(
                    request.getMarks(),
                    exam.getMaxMarks()
            );

            result.setMarks(
                    request.getMarks()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );

            calculateResultValues(
                    result
            );
        }


        if (request.getRemarks() != null) {

            result.setRemarks(
                    normalize(
                            request.getRemarks()
                    )
            );
        }


        if (request.getActive() != null) {

            result.setActive(
                    request.getActive()
            );
        }


        ExamResult updated =
                examResultRepository.save(
                        result
                );


        return toResultResponse(
                examResultRepository
                        .findById(updated.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Result not found after update"
                                ))
        );
    }


    // =========================================================
    // DEACTIVATE RESULT
    // =========================================================

    @Override
    public void deactivateResult(
            Long resultId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)
                && !"TRAINER".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN, ADMIN and assigned TRAINER can deactivate results"
            );
        }


        ExamResult result =
                examResultRepository
                        .findById(resultId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Result not found with id: "
                                                + resultId
                                )
                        );


        Exam exam =
                result.getExam();


        Long organizationId =
                exam.getOrganization().getId();


        if ("ADMIN".equals(role)
                || "TRAINER".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );
        }


        if ("TRAINER".equals(role)) {

            if (exam.getBatch().getTrainer() == null
                    || !exam.getBatch()
                    .getTrainer()
                    .getId()
                    .equals(
                            currentUser.getId()
                    )) {

                throw new IllegalArgumentException(
                        "You can deactivate results only for your assigned batches"
                );
            }
        }


        result.setActive(false);

        examResultRepository.save(result);
    }


    // =========================================================
    // FIND STUDENT EXAMS
    // =========================================================

    private List<Exam> findStudentExams(
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


        Set<Long> batchIds =
                new HashSet<>();


        for (Enrollment enrollment : enrollments) {

            if (enrollment.getStatus()
                    == EnrollmentStatus.ENROLLED
                    && enrollment.getBatch() != null) {

                batchIds.add(
                        enrollment.getBatch().getId()
                );
            }
        }


        if (batchIds.isEmpty()) {

            return new ArrayList<>();
        }


        return examRepository
                .findAllByOrganization_IdAndBatch_IdIn(
                        organizationId,
                        batchIds
                )
                .stream()
                .filter(
                        Exam::isActive
                )
                .toList();
    }


    // =========================================================
    // EXAM VIEW ACCESS
    // =========================================================

    private void validateExamViewAccess(
            Exam exam,
            User currentUser,
            String role
    ) {

        Long organizationId =
                exam.getOrganization().getId();


        if ("SUPER_ADMIN".equals(role)) {

            return;
        }


        if ("ADMIN".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );

            return;
        }


        if ("TRAINER".equals(role)) {

            requireOrganization(
                    currentUser,
                    organizationId
            );


            if (exam.getBatch().getTrainer() == null
                    || !exam.getBatch()
                    .getTrainer()
                    .getId()
                    .equals(
                            currentUser.getId()
                    )) {

                throw new IllegalArgumentException(
                        "You are not assigned to this exam's batch"
                );
            }

            return;
        }


        if ("STUDENT".equals(role)) {

            if (!isStudentEnrolled(
                    currentUser,
                    exam.getBatch().getId()
            )) {

                throw new IllegalArgumentException(
                        "You are not enrolled in this exam's batch"
                );
            }

            return;
        }


        throw new IllegalArgumentException(
                "You are not authorized to view this exam"
        );
    }


    // =========================================================
    // FIND ACCESSIBLE EXAM FOR ADMIN
    // =========================================================

    private Exam findAccessibleExam(
            Long examId,
            User currentUser
    ) {

        String role =
                currentUser.getRole().getName();


        if ("SUPER_ADMIN".equals(role)) {

            return examRepository
                    .findById(examId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Exam not found with id: "
                                            + examId
                            )
                    );
        }


        Long organizationId =
                getOrganizationId(currentUser);


        return examRepository
                .findByIdAndOrganization_Id(
                        examId,
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Exam not found in your organization"
                        ));
    }


    // =========================================================
    // ADMIN ACCESS
    // =========================================================

    private void validateAdminAccess(
            User user
    ) {

        String role =
                user.getRole().getName();


        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN and ADMIN can manage exams"
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
                currentUser.getRole().getName()
        )) {

            return requestedOrganizationId;
        }


        Long currentOrganizationId =
                getOrganizationId(currentUser);


        if (!currentOrganizationId.equals(
                requestedOrganizationId
        )) {

            throw new IllegalArgumentException(
                    "You cannot manage exams for another organization"
            );
        }


        return currentOrganizationId;
    }


    private void requireOrganization(
            User currentUser,
            Long organizationId
    ) {

        if (!getOrganizationId(
                currentUser
        ).equals(organizationId)) {

            throw new IllegalArgumentException(
                    "You cannot access another organization's data"
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
                    "Cannot create exam for a cancelled batch"
            );
        }


        if (batch.getStatus()
                == BatchStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Cannot create exam for a completed batch"
            );
        }
    }


    // =========================================================
    // STUDENT VALIDATION
    // =========================================================

    private void validateStudent(
            User student,
            Long organizationId
    ) {

        if (student.getRole() == null
                || !"STUDENT".equals(
                student.getRole().getName()
        )) {

            throw new IllegalArgumentException(
                    "Selected user is not a STUDENT"
            );
        }


        if (student.getStatus()
                != UserStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Student is not active"
            );
        }


        if (student.getOrganization() == null
                || !organizationId.equals(
                student.getOrganization().getId()
        )) {

            throw new IllegalArgumentException(
                    "Student does not belong to this organization"
            );
        }
    }


    // =========================================================
    // STUDENT ACCESS
    // =========================================================

    private void validateStudentAccess(
            User student,
            User currentUser,
            String role
    ) {

        validateStudent(
                student,
                student.getOrganization()
                        .getId()
        );


        if ("SUPER_ADMIN".equals(role)) {

            return;
        }


        if ("ADMIN".equals(role)) {

            requireOrganization(
                    currentUser,
                    student.getOrganization()
                            .getId()
            );

            return;
        }


        if ("STUDENT".equals(role)) {

            if (!student.getId()
                    .equals(currentUser.getId())) {

                throw new IllegalArgumentException(
                        "Students can access only their own results"
                );
            }

            return;
        }


        throw new IllegalArgumentException(
                "You are not authorized to view student results"
        );
    }


    // =========================================================
    // ENROLLMENT CHECK
    // =========================================================

    private boolean isStudentEnrolled(
            User student,
            Long batchId
    ) {

        if (student.getOrganization() == null) {

            return false;
        }


        List<Enrollment> enrollments =
                enrollmentRepository
                        .findAllByOrganization_IdAndStudent_Id(
                                student.getOrganization()
                                        .getId(),
                                student.getId()
                        );


        return enrollments.stream()
                .anyMatch(
                        enrollment ->
                                enrollment.getBatch() != null
                                        && enrollment.getBatch()
                                        .getId()
                                        .equals(batchId)
                                        && enrollment.getStatus()
                                        == EnrollmentStatus.ENROLLED
                );
    }


    // =========================================================
    // MARKS VALIDATION
    // =========================================================

    private void validateMarks(
            BigDecimal marks,
            BigDecimal maxMarks
    ) {

        if (marks == null) {

            throw new IllegalArgumentException(
                    "Marks are required"
            );
        }


        if (marks.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    "Marks cannot be negative"
            );
        }


        if (marks.compareTo(
                maxMarks
        ) > 0) {

            throw new IllegalArgumentException(
                    "Marks cannot exceed maximum marks: "
                            + maxMarks
            );
        }
    }


    // =========================================================
    // CALCULATE RESULT
    // =========================================================

    private void calculateResultValues(
            ExamResult result
    ) {

        BigDecimal percentage =
                result.getMarks()
                        .divide(
                                result.getExam()
                                        .getMaxMarks(),
                                4,
                                RoundingMode.HALF_UP
                        )
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        result.setPercentage(
                percentage
        );


        result.setGrade(
                calculateGrade(
                        percentage
                )
        );
    }


    private String calculateGrade(
            BigDecimal percentage
    ) {

        if (percentage.compareTo(
                BigDecimal.valueOf(90)
        ) >= 0) {

            return "A+";
        }

        if (percentage.compareTo(
                BigDecimal.valueOf(80)
        ) >= 0) {

            return "A";
        }

        if (percentage.compareTo(
                BigDecimal.valueOf(70)
        ) >= 0) {

            return "B";
        }

        if (percentage.compareTo(
                BigDecimal.valueOf(60)
        ) >= 0) {

            return "C";
        }

        if (percentage.compareTo(
                BigDecimal.valueOf(50)
        ) >= 0) {

            return "D";
        }

        return "F";
    }


    // =========================================================
    // EXAM RESPONSE
    // =========================================================

    private ExamResponse toExamResponse(
            Exam exam
    ) {

        Batch batch =
                exam.getBatch();


        Long trainerId = null;

        String trainerName = null;


        if (batch != null
                && batch.getTrainer() != null) {

            trainerId =
                    batch.getTrainer().getId();

            trainerName =
                    (
                            batch.getTrainer()
                                    .getFirstName()
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


        return new ExamResponse(
                exam.getId(),
                exam.getName(),
                exam.getDescription(),
                exam.getExamDate(),
                exam.getMaxMarks(),
                exam.getStatus(),
                exam.isActive(),
                exam.getOrganization() != null
                        ? exam.getOrganization().getId()
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
                        && batch.getCourse() != null
                        ? batch.getCourse().getId()
                        : null,
                batch != null
                        && batch.getCourse() != null
                        ? batch.getCourse().getName()
                        : null,
                trainerId,
                trainerName
        );
    }


    // =========================================================
    // RESULT RESPONSE
    // =========================================================

    private ExamResultResponse toResultResponse(
            ExamResult result
    ) {

        Exam exam =
                result.getExam();

        Batch batch =
                exam.getBatch();

        User student =
                result.getStudent();


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


        return new ExamResultResponse(
                result.getId(),
                exam.getId(),
                exam.getName(),
                exam.getExamDate(),
                exam.getMaxMarks(),
                student.getId(),
                studentName,
                student.getEmail(),
                result.getMarks(),
                result.getPercentage(),
                result.getGrade(),
                result.getRemarks(),
                result.isActive(),
                result.getOrganization() != null
                        ? result.getOrganization().getId()
                        : null,
                batch != null
                        ? batch.getId()
                        : null,
                batch != null
                        ? batch.getName()
                        : null,
                batch != null
                        && batch.getCourse() != null
                        ? batch.getCourse().getId()
                        : null,
                batch != null
                        && batch.getCourse() != null
                        ? batch.getCourse().getName()
                        : null
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
}