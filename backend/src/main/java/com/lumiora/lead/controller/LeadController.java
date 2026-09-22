package com.lumiora.lead.controller;

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

import com.lumiora.course.entity.Course;
import com.lumiora.course.service.CourseService;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.lead.dto.LeadCreateRequest;
import com.lumiora.lead.dto.LeadResponse;
import com.lumiora.lead.dto.LeadUpdateRequest;
import com.lumiora.lead.entity.Lead;
import com.lumiora.lead.entity.LeadStatus;
import com.lumiora.lead.service.LeadService;

import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;

import com.lumiora.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    private final UserService userService;

    private final CourseService courseService;

    private final OrganizationRepository organizationRepository;


    // =========================================================
    // CREATE LEAD
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createLead(
            @Valid @RequestBody LeadCreateRequest request,
            Authentication authentication) {

        boolean isSuperAdmin =
                isSuperAdmin(authentication);

        Organization organization;

        if (isSuperAdmin) {

            organization =
                    organizationRepository
                            .findById(
                                    request.getOrganizationId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Organization not found: "
                                                    + request.getOrganizationId()
                                    ));

        } else {

            User currentUser =
                    getCurrentUser(authentication);

            organization =
                    validateOrganization(
                            currentUser,
                            request.getOrganizationId()
                    );
        }

        if (request.getEmail() != null &&
                !request.getEmail().isBlank() &&
                leadService.existsByEmailAndOrganizationId(
                        request.getEmail(),
                        organization.getId())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "A lead with this email already exists in this organization"
                            )
                    );
        }

        if (leadService.existsByPhoneAndOrganizationId(
                request.getPhone(),
                organization.getId())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "A lead with this phone number already exists in this organization"
                            )
                    );
        }


        Course course = null;

        if (request.getCourseId() != null) {

            course =
                    courseService
                            .findByIdAndOrganizationId(
                                    request.getCourseId(),
                                    organization.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Course not found in this organization: "
                                                    + request.getCourseId()
                                    ));
        }


        User counselor = null;

        if (request.getCounselorId() != null) {

            counselor =
                    validateCounselor(
                            request.getCounselorId(),
                            organization.getId()
                    );

        } else if (
                hasRole(authentication, "COUNSELOR")) {

            counselor =
                    validateCounselor(
                            getCurrentUser(authentication).getId(),
                            organization.getId()
                    );
        }


        Lead lead = new Lead();

        lead.setFirstName(
                request.getFirstName()
        );

        lead.setLastName(
                request.getLastName()
        );

        lead.setEmail(
                request.getEmail()
        );

        lead.setPhone(
                request.getPhone()
        );

        lead.setNotes(
                request.getNotes()
        );

        lead.setFollowUpDate(
                request.getFollowUpDate()
        );

        lead.setSource(
                request.getSource()
        );

        lead.setStatus(
                LeadStatus.NEW
        );

        lead.setActive(true);

        lead.setOrganization(
                organization
        );

        lead.setInterestedCourse(
                course
        );

        lead.setCounselor(
                counselor
        );

        Lead savedLead =
                leadService.save(lead);

        Lead refreshedLead =
                leadService
                        .findById(savedLead.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Lead not found after creation"
                                ));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Lead created successfully",
                                toResponse(refreshedLead)
                        )
                );
    }


    // =========================================================
    // GET ALL LEADS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeadResponse>>>
            getAllLeads(
                    Authentication authentication) {

        List<Lead> leads;

        User currentUser =
                getCurrentUser(authentication);

        if (isSuperAdmin(authentication)) {

            leads = leadService.findAll();

        } else {

            if (currentUser.getOrganization() == null) {

                throw new IllegalStateException(
                        "User is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            if (hasRole(authentication, "COUNSELOR")) {

                leads =
                        leadService
                                .findAllByOrganizationAndCounselor(
                                        organizationId,
                                        currentUser.getId()
                                );

            } else {

                leads =
                        leadService
                                .findAllByOrganizationId(
                                        organizationId
                                );
            }
        }

        List<LeadResponse> responses =
                leads.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Leads fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET LEAD BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>>
            getLeadById(
                    @PathVariable Long id,
                    Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        Lead lead;

        if (isSuperAdmin(authentication)) {

            lead =
                    leadService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Lead not found with id: "
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

            if (hasRole(authentication, "COUNSELOR")) {

                lead =
                        leadService
                                .findByIdAndOrganizationAndCounselor(
                                        id,
                                        organizationId,
                                        currentUser.getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Lead not found or not assigned to you: "
                                                        + id
                                        ));

            } else {

                lead =
                        leadService
                                .findByIdAndOrganizationId(
                                        id,
                                        organizationId
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Lead not found with id: "
                                                        + id
                                        ));
            }
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lead fetched successfully",
                        toResponse(lead)
                )
        );
    }


    // =========================================================
    // UPDATE LEAD
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadUpdateRequest request,
            Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        Lead lead;

        if (isSuperAdmin(authentication)) {

            lead =
                    leadService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Lead not found with id: "
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

            if (hasRole(authentication, "COUNSELOR")) {

                lead =
                        leadService
                                .findByIdAndOrganizationAndCounselor(
                                        id,
                                        organizationId,
                                        currentUser.getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Lead not found or not assigned to you: "
                                                        + id
                                        ));

            } else {

                lead =
                        leadService
                                .findByIdAndOrganizationId(
                                        id,
                                        organizationId
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Lead not found with id: "
                                                        + id
                                        ));
            }
        }

        if (request.getFirstName() != null &&
                !request.getFirstName().isBlank()) {

            lead.setFirstName(
                    request.getFirstName()
            );
        }

        if (request.getLastName() != null) {

            lead.setLastName(
                    request.getLastName()
            );
        }

        if (request.getEmail() != null) {

            lead.setEmail(
                    request.getEmail()
            );
        }

        if (request.getPhone() != null &&
                !request.getPhone().isBlank()) {

            lead.setPhone(
                    request.getPhone()
            );
        }

        if (request.getNotes() != null) {

            lead.setNotes(
                    request.getNotes()
            );
        }

        if (request.getFollowUpDate() != null) {

            lead.setFollowUpDate(
                    request.getFollowUpDate()
            );
        }

        if (request.getSource() != null) {

            lead.setSource(
                    request.getSource()
            );
        }

        if (request.getStatus() != null) {

            lead.setStatus(
                    request.getStatus()
            );
        }

        Long organizationId =
                lead.getOrganization().getId();

        if (request.getCourseId() != null) {

            Course course =
                    courseService
                            .findByIdAndOrganizationId(
                                    request.getCourseId(),
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Course not found in this organization: "
                                                    + request.getCourseId()
                                    ));

            lead.setInterestedCourse(course);
        }

        if (request.getCounselorId() != null) {

            if (hasRole(authentication, "COUNSELOR") &&
                    !isSuperAdmin(authentication)) {

                if (!request.getCounselorId()
                        .equals(currentUser.getId())) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                    ApiResponse.failure(
                                            "A counselor can only keep the lead assigned to themselves"
                                    )
                            );
                }
            }

            User counselor =
                    validateCounselor(
                            request.getCounselorId(),
                            organizationId
                    );

            lead.setCounselor(counselor);
        }

        if (request.getStatus()
                == LeadStatus.CONVERTED) {

            lead.setActive(true);
        }

        Lead updatedLead =
                leadService.save(lead);

        Lead refreshedLead =
                leadService
                        .findById(updatedLead.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Lead not found after update"
                                ));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lead updated successfully",
                        toResponse(refreshedLead)
                )
        );
    }


    // =========================================================
    // DEACTIVATE LEAD
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deactivateLead(
                    @PathVariable Long id,
                    Authentication authentication) {

        User currentUser =
                getCurrentUser(authentication);

        Lead lead;

        if (isSuperAdmin(authentication)) {

            lead =
                    leadService
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Lead not found with id: "
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

            if (hasRole(authentication, "COUNSELOR")) {

                lead =
                        leadService
                                .findByIdAndOrganizationAndCounselor(
                                        id,
                                        organizationId,
                                        currentUser.getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Lead not found or not assigned to you: "
                                                        + id
                                        ));

            } else {

                lead =
                        leadService
                                .findByIdAndOrganizationId(
                                        id,
                                        organizationId
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Lead not found with id: "
                                                        + id
                                        ));
            }
        }

        lead.setActive(false);

        lead.setStatus(
                LeadStatus.LOST
        );

        leadService.save(lead);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lead deactivated successfully",
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
    // ORGANIZATION VALIDATION
    // =========================================================

    private Organization validateOrganization(
            User currentUser,
            Long requestedOrganizationId) {

        if (currentUser.getOrganization() == null) {

            throw new IllegalStateException(
                    "User is not assigned to an organization"
            );
        }

        Long currentOrganizationId =
                currentUser.getOrganization().getId();

        if (!currentOrganizationId.equals(
                requestedOrganizationId)) {

            throw new IllegalArgumentException(
                    "You cannot create a lead for another organization"
            );
        }

        return currentUser.getOrganization();
    }


    // =========================================================
    // COUNSELOR VALIDATION
    // =========================================================

    private User validateCounselor(
            Long counselorId,
            Long organizationId) {

        User counselor =
                userService
                        .findByIdAndOrganizationId(
                                counselorId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "Counselor not found in this organization: "
                                                + counselorId
                                ));

        if (counselor.getRole() == null ||
                !"COUNSELOR".equals(
                        counselor.getRole().getName())) {

            throw new IllegalArgumentException(
                    "Selected user is not a COUNSELOR"
            );
        }

        if (counselor.getStatus()
                != UserStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Counselor is not active"
            );
        }

        return counselor;
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
                                .equals("ROLE_" + role)
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
    // RESPONSE MAPPING
    // =========================================================

    private LeadResponse toResponse(
            Lead lead) {

        String counselorName = null;

        Long counselorId = null;

        if (lead.getCounselor() != null) {

            counselorId =
                    lead.getCounselor().getId();

            counselorName =
                    (
                            lead.getCounselor().getFirstName()
                                    + " "
                                    + (
                                    lead.getCounselor().getLastName() != null
                                            ? lead.getCounselor().getLastName()
                                            : ""
                            )
                    ).trim();
        }

        return new LeadResponse(
                lead.getId(),
                lead.getFirstName(),
                lead.getLastName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getNotes(),
                lead.getFollowUpDate(),
                lead.getStatus(),
                lead.getSource(),
                lead.isActive(),
                lead.getOrganization() != null
                        ? lead.getOrganization().getId()
                        : null,
                lead.getInterestedCourse() != null
                        ? lead.getInterestedCourse().getId()
                        : null,
                lead.getInterestedCourse() != null
                        ? lead.getInterestedCourse().getName()
                        : null,
                counselorId,
                counselorName
        );
    }
}