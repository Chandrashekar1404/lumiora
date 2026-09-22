package com.lumiora.leadconversion.service;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.entity.BatchStatus;
import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.lead.entity.Lead;
import com.lumiora.lead.entity.LeadStatus;
import com.lumiora.lead.repository.LeadRepository;
import com.lumiora.leadconversion.dto.LeadConversionRequest;
import com.lumiora.leadconversion.dto.LeadConversionResponse;
import com.lumiora.organization.entity.Organization;
import com.lumiora.entity.auth.Role;
import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;
import com.lumiora.repository.RoleRepository;
import com.lumiora.repository.UserRepository;
import com.lumiora.batch.repository.BatchRepository;
import com.lumiora.enrollment.repository.EnrollmentRepository;

@Service
public class LeadConversionServiceImpl implements LeadConversionService {

    private final LeadRepository leadRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    public LeadConversionServiceImpl(
            LeadRepository leadRepository,
            BatchRepository batchRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EnrollmentRepository enrollmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.leadRepository = leadRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LeadConversionResponse convertLead(
            Long leadId,
            LeadConversionRequest request
    ) {

        User currentUser = getCurrentUser();

        String currentRole = currentUser.getRole().getName();

        Lead lead;

        /*
         * SUPER_ADMIN
         */
        if ("SUPER_ADMIN".equals(currentRole)) {

            lead = leadRepository.findById(leadId)
                    .orElseThrow(() ->
                            new RuntimeException("Lead not found"));

        }

        /*
         * ADMIN
         */
        else if ("ADMIN".equals(currentRole)) {

            if (currentUser.getOrganization() == null) {
                throw new RuntimeException(
                        "Current admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            lead = leadRepository
                    .findByIdAndOrganization_Id(
                            leadId,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new RuntimeException("Lead not found"));
        }

        /*
         * COUNSELOR
         */
        else if ("COUNSELOR".equals(currentRole)) {

            if (currentUser.getOrganization() == null) {
                throw new RuntimeException(
                        "Counselor is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            lead = leadRepository
                    .findByIdAndOrganization_IdAndCounselor_Id(
                            leadId,
                            organizationId,
                            currentUser.getId()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Lead not found or not assigned to this counselor"
                            ));
        }

        else {
            throw new RuntimeException(
                    "You are not authorized to convert leads"
            );
        }

        /*
         * Only NEW/CONTACTED/INTERESTED/FOLLOW_UP leads
         * should normally reach conversion.
         */
        if (LeadStatus.CONVERTED.equals(lead.getStatus())) {
            throw new RuntimeException(
                    "Lead is already converted"
            );
        }

        /*
         * Email is required because the created student
         * needs a login account.
         */
        if (lead.getEmail() == null || lead.getEmail().isBlank()) {
            throw new RuntimeException(
                    "Lead email is required for conversion"
            );
        }

        /*
         * Find organization
         */
        Organization organization = lead.getOrganization();

        if (organization == null) {
            throw new RuntimeException(
                    "Lead is not associated with an organization"
            );
        }

        /*
         * Find batch
         */
        Batch batch = batchRepository
                .findByIdAndOrganization_Id(
                        request.getBatchId(),
                        organization.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Batch not found in this organization"
                        ));

        /*
         * Batch must be active.
         */
        if (!batch.isActive()) {
            throw new RuntimeException(
                    "Selected batch is inactive"
            );
        }

        /*
         * Batch cannot be cancelled or completed.
         */
        if (BatchStatus.CANCELLED.equals(batch.getStatus())) {
            throw new RuntimeException(
                    "Cannot convert lead to a cancelled batch"
            );
        }

        if (BatchStatus.COMPLETED.equals(batch.getStatus())) {
            throw new RuntimeException(
                    "Cannot convert lead to a completed batch"
            );
        }

        /*
         * If the lead already has a course,
         * ensure the selected batch belongs to that course.
         */
        if (lead.getInterestedCourse() != null
                && batch.getCourse() != null
                && !lead.getInterestedCourse()
                .getId()
                .equals(batch.getCourse().getId())) {

            throw new RuntimeException(
                    "Selected batch does not belong to the lead's interested course"
            );
        }

        /*
         * Prevent duplicate student account.
         */
        if (userRepository.findByEmail(lead.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "A user already exists with lead email: "
                            + lead.getEmail()
            );
        }

        /*
         * Prevent duplicate phone account.
         */
        if (lead.getPhone() != null
                && userRepository.findByPhone(lead.getPhone()).isPresent()) {

            throw new RuntimeException(
                    "A user already exists with lead phone: "
                            + lead.getPhone()
            );
        }

        /*
         * Get STUDENT role
         */
        Role studentRole = roleRepository
                .findByName("STUDENT")
                .orElseThrow(() ->
                        new RuntimeException(
                                "STUDENT role not found"
                        ));

        /*
         * Check batch capacity.
         *
         * We count only currently ENROLLED students.
         */
        if (batch.getCapacity() != null) {

            long enrolledCount =
                    enrollmentRepository
                            .countByBatch_IdAndStatus(
                                    batch.getId(),
                                    EnrollmentStatus.ENROLLED
                            );

            if (enrolledCount >= batch.getCapacity()) {
                throw new RuntimeException(
                        "Selected batch is already full"
                );
            }
        }

        /*
         * 1. Create student user
         */
        User student = new User();

        student.setFirstName(lead.getFirstName());
        student.setLastName(lead.getLastName());
        student.setEmail(lead.getEmail());
        student.setPhone(lead.getPhone());
        student.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        student.setRole(studentRole);
        student.setOrganization(organization);
        student.setStatus(UserStatus.ACTIVE);

        student = userRepository.save(student);

        /*
         * 2. Create enrollment
         */
        Enrollment enrollment = new Enrollment();

        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setOrganization(organization);
        enrollment.setBatch(batch);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);

        enrollment = enrollmentRepository.save(enrollment);

        /*
         * 3. Mark lead as converted
         */
        lead.setStatus(LeadStatus.CONVERTED);

        leadRepository.save(lead);

        /*
         * 4. Prepare response
         */
        LeadConversionResponse response =
                new LeadConversionResponse();

        response.setLeadId(lead.getId());

        response.setStudentId(student.getId());

        response.setStudentName(
                student.getFirstName()
                        + " "
                        + (student.getLastName() == null
                        ? ""
                        : student.getLastName())
        );

        response.setStudentEmail(student.getEmail());

        response.setBatchId(batch.getId());
        response.setBatchName(batch.getName());

        response.setEnrollmentId(enrollment.getId());

        response.setEnrollmentStatus(
                enrollment.getStatus().name()
        );

        response.setLeadStatus(
                lead.getStatus().name()
        );

        return response;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Current user not found"
                        ));
    }
}