package com.lumiora.organization.controller;

import com.lumiora.dto.organization.OrganizationCreateRequest;
import com.lumiora.dto.organization.OrganizationResponse;
import com.lumiora.dto.organization.OrganizationUpdateRequest;
import com.lumiora.dto.response.ApiResponse;
import com.lumiora.exception.UserNotFoundException;
import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.service.OrganizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<?> createOrganization(
            @Valid @RequestBody OrganizationCreateRequest request) {

        if (organizationService.existsByName(request.getName())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "An organization with this name already exists"
                            )
                    );
        }

        if (organizationService.existsByCode(request.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "An organization with this code already exists"
                            )
                    );
        }

        Organization organization = new Organization();

        organization.setName(request.getName());
        organization.setCode(request.getCode());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setAddress(request.getAddress());
        organization.setActive(true);

        Organization savedOrganization =
                organizationService.save(organization);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Organization created successfully",
                                toResponse(savedOrganization)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {

        List<OrganizationResponse> organizations =
                organizationService.findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organizations fetched successfully",
                        organizations
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(
            @PathVariable Long id) {

        Organization organization =
                organizationService.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found with id: " + id
                                )
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organization fetched successfully",
                        toResponse(organization)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationUpdateRequest request) {

        Organization organization =
                organizationService.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found with id: " + id
                                )
                        );

        if (request.getName() != null &&
                !request.getName().isBlank() &&
                !request.getName().equals(organization.getName()) &&
                organizationService.existsByName(request.getName())) {

            throw new IllegalArgumentException(
                    "An organization with this name already exists"
            );
        }

        organization.setName(request.getName());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setAddress(request.getAddress());

        Organization updatedOrganization =
                organizationService.update(organization);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organization updated successfully",
                        toResponse(updatedOrganization)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateOrganization(
            @PathVariable Long id) {

        Organization organization =
                organizationService.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found with id: " + id
                                )
                        );

        organization.setActive(false);

        organizationService.update(organization);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organization deactivated successfully",
                        null
                )
        );
    }

    private OrganizationResponse toResponse(
            Organization organization) {

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getCode(),
                organization.getEmail(),
                organization.getPhone(),
                organization.getAddress(),
                organization.isActive()
        );
    }
}