package com.lumiora.certificate.service.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumiora.batch.entity.Batch;
import com.lumiora.certificate.dto.CertificateCreateRequest;
import com.lumiora.certificate.dto.CertificateResponse;
import com.lumiora.certificate.dto.CertificateUpdateRequest;
import com.lumiora.certificate.entity.Certificate;
import com.lumiora.certificate.entity.CertificateStatus;
import com.lumiora.certificate.repository.CertificateRepository;
import com.lumiora.course.entity.Course;
import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;
import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.repository.EnrollmentRepository;
import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateServiceImpl
        implements com.lumiora.certificate.service.CertificateService {

    private final CertificateRepository certificateRepository;

    private final EnrollmentRepository enrollmentRepository;

    private final UserRepository userRepository;

    // =========================================================
    // ISSUE CERTIFICATE
    // =========================================================

    @Override
    public CertificateResponse issueCertificate(
            CertificateCreateRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        validateIssuerAccess(currentUser);

        Enrollment enrollment =
                enrollmentRepository
                        .findById(request.getEnrollmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Enrollment not found with id: "
                                                + request.getEnrollmentId()
                                )
                        );

        validateEnrollment(enrollment);

        Long organizationId =
                enrollment.getOrganization().getId();

        requireOrganization(
                currentUser,
                organizationId
        );

        if (certificateRepository
                .existsByEnrollment_Id(enrollment.getId())) {

            throw new IllegalArgumentException(
                    "A certificate has already been issued for this enrollment"
            );
        }

        User student = enrollment.getStudent();

        Course course =
                enrollment.getBatch().getCourse();

        Batch batch =
                enrollment.getBatch();

        Certificate certificate =
                new Certificate();

        certificate.setCertificateNumber(
                generateCertificateNumber()
        );

        certificate.setIssueDate(
                LocalDate.now()
        );

        certificate.setAchievement(
                normalize(request.getAchievement())
        );

        certificate.setRemarks(
                normalize(request.getRemarks())
        );

        certificate.setStatus(
                CertificateStatus.ISSUED
        );

        certificate.setActive(true);

        certificate.setEnrollment(
                enrollment
        );

        certificate.setStudent(
                student
        );

        certificate.setCourse(
                course
        );

        certificate.setBatch(
                batch
        );

        certificate.setOrganization(
                enrollment.getOrganization()
        );

        certificate.setIssuedBy(
                currentUser
        );

        Certificate saved =
                certificateRepository.save(certificate);

        return toResponse(saved);
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getAllCertificates(
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                getRole(currentUser);

        List<Certificate> certificates;

        if ("SUPER_ADMIN".equals(role)) {

            certificates =
                    certificateRepository.findAll();

        } else if ("ADMIN".equals(role)) {

            certificates =
                    certificateRepository
                            .findAllByOrganization_Id(
                                    getOrganizationId(
                                            currentUser
                                    )
                            );

        } else if ("TRAINER".equals(role)) {

            certificates =
                    certificateRepository
                            .findAllByOrganization_Id(
                                    getOrganizationId(
                                            currentUser
                                    )
                            )
                            .stream()
                            .filter(certificate ->
                                    certificate.getBatch()
                                            .getTrainer() != null
                                            && certificate
                                                    .getBatch()
                                                    .getTrainer()
                                                    .getId()
                                                    .equals(
                                                            currentUser.getId()
                                                    )
                            )
                            .toList();

        } else if ("STUDENT".equals(role)) {

            certificates =
                    certificateRepository
                            .findAllByStudent_IdAndOrganization_Id(
                                    currentUser.getId(),
                                    getOrganizationId(
                                            currentUser
                                    )
                            );

        } else {

            throw new IllegalArgumentException(
                    "You are not authorized to view certificates"
            );
        }

        return certificates
                .stream()
                .filter(Certificate::isActive)
                .sorted(
                        Comparator
                                .comparing(
                                        Certificate::getIssueDate
                                )
                                .reversed()
                )
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // MY CERTIFICATES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates(
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        if (!"STUDENT".equals(
                getRole(currentUser)
        )) {

            throw new IllegalArgumentException(
                    "My certificates are available only to students"
            );
        }

        return certificateRepository
                .findAllByStudent_IdAndOrganization_Id(
                        currentUser.getId(),
                        getOrganizationId(currentUser)
                )
                .stream()
                .filter(Certificate::isActive)
                .sorted(
                        Comparator
                                .comparing(
                                        Certificate::getIssueDate
                                )
                                .reversed()
                )
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // CERTIFICATES BY STUDENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getCertificatesByStudent(
            Long studentId,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        User student =
                userRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found with id: "
                                                + studentId
                                )
                        );

        validateStudent(student);

        validateStudentViewAccess(
                student,
                currentUser
        );

        return certificateRepository
                .findAllByStudent_IdAndOrganization_Id(
                        studentId,
                        student.getOrganization().getId()
                )
                .stream()
                .filter(Certificate::isActive)
                .sorted(
                        Comparator
                                .comparing(
                                        Certificate::getIssueDate
                                )
                                .reversed()
                )
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // CERTIFICATES BY BATCH
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> getCertificatesByBatch(
            Long batchId,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                getRole(currentUser);

        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)
                && !"TRAINER".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN, ADMIN and TRAINER can view batch certificates"
            );
        }

        Long organizationId =
                getOrganizationId(currentUser);

        List<Certificate> certificates =
                certificateRepository
                        .findAllByBatch_IdAndOrganization_Id(
                                batchId,
                                organizationId
                        );

        if ("TRAINER".equals(role)) {

            certificates =
                    certificates
                            .stream()
                            .filter(certificate ->
                                    certificate
                                            .getBatch()
                                            .getTrainer() != null
                                            && certificate
                                                    .getBatch()
                                                    .getTrainer()
                                                    .getId()
                                                    .equals(
                                                            currentUser.getId()
                                                    )
                            )
                            .toList();
        }

        return certificates
                .stream()
                .filter(Certificate::isActive)
                .sorted(
                        Comparator
                                .comparing(
                                        Certificate::getIssueDate
                                )
                                .reversed()
                )
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(
            Long certificateId,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        Certificate certificate =
                findCertificate(certificateId);

        validateCertificateViewAccess(
                certificate,
                currentUser
        );

        return toResponse(certificate);
    }

    // =========================================================
    // PUBLIC VERIFICATION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse verifyCertificate(
            String certificateNumber) {

        if (certificateNumber == null
                || certificateNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Certificate number is required"
            );
        }

        Certificate certificate =
                certificateRepository
                        .findByCertificateNumber(
                                certificateNumber.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Certificate not found"
                                )
                        );

        if (!certificate.isActive()
                || certificate.getStatus()
                        == CertificateStatus.REVOKED) {

            throw new IllegalArgumentException(
                    "Certificate is revoked or inactive"
            );
        }

        return toResponse(certificate);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public CertificateResponse updateCertificate(
            Long certificateId,
            CertificateUpdateRequest request,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        validateIssuerAccess(currentUser);

        Certificate certificate =
                findCertificate(certificateId);

        requireOrganization(
                currentUser,
                certificate.getOrganization().getId()
        );

        if (request.getAchievement() != null) {

            certificate.setAchievement(
                    normalize(
                            request.getAchievement()
                    )
            );
        }

        if (request.getRemarks() != null) {

            certificate.setRemarks(
                    normalize(
                            request.getRemarks()
                    )
            );
        }

        if (request.getStatus() != null) {

            certificate.setStatus(
                    request.getStatus()
            );

            if (request.getStatus()
                    == CertificateStatus.REVOKED) {

                certificate.setActive(false);
            }
        }

        if (request.getActive() != null) {

            certificate.setActive(
                    request.getActive()
            );

            if (request.getActive()) {

                certificate.setStatus(
                        CertificateStatus.ISSUED
                );
            }
        }

        return toResponse(
                certificateRepository.save(
                        certificate
                )
        );
    }

    // =========================================================
    // REVOKE
    // =========================================================

    @Override
    public void revokeCertificate(
            Long certificateId,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        validateIssuerAccess(currentUser);

        Certificate certificate =
                findCertificate(certificateId);

        requireOrganization(
                currentUser,
                certificate.getOrganization().getId()
        );

        certificate.setStatus(
                CertificateStatus.REVOKED
        );

        certificate.setActive(false);

        certificateRepository.save(certificate);
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateIssuerAccess(
            User user) {

        String role =
                getRole(user);

        if (!"SUPER_ADMIN".equals(role)
                && !"ADMIN".equals(role)) {

            throw new IllegalArgumentException(
                    "Only SUPER_ADMIN and ADMIN can issue or manage certificates"
            );
        }
    }

    private void validateEnrollment(
            Enrollment enrollment) {

        if (enrollment.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "Enrollment is not assigned to an organization"
            );
        }

        if (enrollment.getStudent() == null) {

            throw new IllegalArgumentException(
                    "Enrollment does not have a student"
            );
        }

        if (enrollment.getBatch() == null) {

            throw new IllegalArgumentException(
                    "Enrollment does not have a batch"
            );
        }

        if (enrollment.getBatch().getCourse() == null) {

            throw new IllegalArgumentException(
                    "Batch does not have a course"
            );
        }

        if (enrollment.getStatus()
                != EnrollmentStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Certificate can be issued only for completed enrollments"
            );
        }

        if (enrollment.getStudent().getStatus()
                != UserStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Student account is inactive"
            );
        }

        if (!"STUDENT".equals(
                getRole(enrollment.getStudent())
        )) {

            throw new IllegalArgumentException(
                    "Enrollment student is not a STUDENT"
            );
        }
    }

    private void validateStudent(
            User student) {

        if (student.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "Student is not assigned to an organization"
            );
        }

        if (!"STUDENT".equals(
                getRole(student)
        )) {

            throw new IllegalArgumentException(
                    "Selected user is not a STUDENT"
            );
        }
    }

    private void validateStudentViewAccess(
            User student,
            User currentUser) {

        String role =
                getRole(currentUser);

        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        requireOrganization(
                currentUser,
                student.getOrganization().getId()
        );

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("STUDENT".equals(role)) {

            if (!student.getId().equals(
                    currentUser.getId()
            )) {

                throw new IllegalArgumentException(
                        "You can view only your own certificates"
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "You are not authorized to view these certificates"
        );
    }

    private void validateCertificateViewAccess(
            Certificate certificate,
            User currentUser) {

        String role =
                getRole(currentUser);

        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        requireOrganization(
                currentUser,
                certificate.getOrganization().getId()
        );

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("TRAINER".equals(role)) {

            if (certificate.getBatch().getTrainer() == null
                    || !certificate.getBatch()
                            .getTrainer()
                            .getId()
                            .equals(
                                    currentUser.getId()
                            )) {

                throw new IllegalArgumentException(
                        "You can view certificates only for your assigned batches"
                );
            }

            return;
        }

        if ("STUDENT".equals(role)) {

            if (!certificate.getStudent()
                    .getId()
                    .equals(
                            currentUser.getId()
                    )) {

                throw new IllegalArgumentException(
                        "You can view only your own certificate"
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "You are not authorized to view this certificate"
        );
    }

    private Certificate findCertificate(
            Long certificateId) {

        return certificateRepository
                .findById(certificateId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Certificate not found with id: "
                                        + certificateId
                        )
                );
    }

    private void requireOrganization(
            User currentUser,
            Long organizationId) {

        if (!getOrganizationId(
                currentUser
        ).equals(organizationId)) {

            throw new IllegalArgumentException(
                    "You cannot access another organization's data"
            );
        }
    }

    private Long getOrganizationId(
            User user) {

        if (user.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "User is not assigned to an organization"
            );
        }

        return user.getOrganization().getId();
    }

    private String getRole(
            User user) {

        if (user.getRole() == null) {

            throw new IllegalArgumentException(
                    "User role is not assigned"
            );
        }

        return user.getRole().getName();
    }

    private User getCurrentUser(
            Authentication authentication) {

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

    private String generateCertificateNumber() {

        String number;

        do {

            number =
                    "LUM-"
                            + LocalDate.now()
                                    .getYear()
                            + "-"
                            + UUID.randomUUID()
                                    .toString()
                                    .replace(
                                            "-",
                                            ""
                                    )
                                    .substring(
                                            0,
                                            12
                                    )
                                    .toUpperCase();

        } while (
                certificateRepository
                        .findByCertificateNumber(
                                number
                        )
                        .isPresent()
        );

        return number;
    }

    private String normalize(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private CertificateResponse toResponse(
            Certificate certificate) {

        User student =
                certificate.getStudent();

        User issuedBy =
                certificate.getIssuedBy();

        Course course =
                certificate.getCourse();

        Batch batch =
                certificate.getBatch();

        return new CertificateResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getIssueDate(),
                certificate.getAchievement(),
                certificate.getRemarks(),
                certificate.getStatus(),
                certificate.isActive(),
                certificate.getEnrollment().getId(),
                student.getId(),
                fullName(student),
                student.getEmail(),
                course.getId(),
                course.getName(),
                batch.getId(),
                batch.getName(),
                batch.getCode(),
                certificate.getOrganization().getId(),
                certificate.getOrganization().getName(),
                issuedBy == null
                        ? null
                        : issuedBy.getId(),
                issuedBy == null
                        ? null
                        : fullName(issuedBy)
        );
    }

    private String fullName(
            User user) {

        String firstName =
                user.getFirstName() == null
                        ? ""
                        : user.getFirstName().trim();

        String lastName =
                user.getLastName() == null
                        ? ""
                        : user.getLastName().trim();

        return (
                firstName
                        + " "
                        + lastName
        ).trim();
    }
}