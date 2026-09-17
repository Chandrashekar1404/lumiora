package com.lumiora.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.dto.user.UserCreateRequest;
import com.lumiora.dto.user.UserResponse;
import com.lumiora.dto.user.UserUpdateRequest;
import com.lumiora.entity.auth.Role;
import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;
import com.lumiora.service.RoleService;
import com.lumiora.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.lumiora.exception.UserNotFoundException;
import com.lumiora.dto.response.ApiResponse;
import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;
        private final RoleService roleService;
        private final PasswordEncoder passwordEncoder;
        private final OrganizationRepository organizationRepository;

        @PostMapping
        public ResponseEntity<?> createUser(
                        @Valid @RequestBody UserCreateRequest request,
                        Authentication authentication) {

                if (userService.existsByEmail(request.getEmail())) {
                        return ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body(
                                                        ApiResponse.failure(
                                                                        "A user with this email already exists"));
                }

                boolean isSuperAdmin = authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority -> authority.getAuthority()
                                                .equals("ROLE_SUPER_ADMIN"));

                if (!isSuperAdmin &&
                                "SUPER_ADMIN".equalsIgnoreCase(request.getRole())) {

                        return ResponseEntity
                                        .status(HttpStatus.FORBIDDEN)
                                        .body(
                                                        ApiResponse.failure(
                                                                        "You cannot assign the SUPER_ADMIN role"));
                }

                Role role = roleService
                                .findByName(request.getRole())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Role not found: " + request.getRole()));

                Organization organization;

                if (isSuperAdmin) {

                        // SUPER_ADMIN can create a user in any organization
                        organization = organizationRepository
                                        .findById(request.getOrganizationId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Organization not found: "
                                                                        + request.getOrganizationId()));

                } else {

                        // Get the logged-in ADMIN
                        User currentUser = userService
                                        .findByEmail(authentication.getName())
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "Logged-in user not found"));

                        if (currentUser.getOrganization() == null) {
                                throw new IllegalStateException(
                                                "Admin is not assigned to an organization");
                        }

                        Long currentOrganizationId = currentUser.getOrganization().getId();

                        // ADMIN cannot create a user in another organization
                        if (!currentOrganizationId.equals(
                                        request.getOrganizationId())) {

                                return ResponseEntity
                                                .status(HttpStatus.FORBIDDEN)
                                                .body(
                                                                ApiResponse.failure(
                                                                                "You cannot create a user for another organization"));
                        }

                        organization = currentUser.getOrganization();
                }

                User user = new User();

                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setEmail(request.getEmail());
                user.setPhone(request.getPhone());
                user.setPassword(
                                passwordEncoder.encode(request.getPassword()));
                user.setRole(role);
                user.setOrganization(organization);
                user.setStatus(UserStatus.ACTIVE);

                User savedUser = userService.save(user);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "User created successfully",
                                                                toResponse(savedUser)));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
                        Authentication authentication) {

                String email = authentication.getName();

                User currentUser = userService.findByEmail(email)
                                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));

                List<User> users;

                boolean isSuperAdmin = authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority -> authority.getAuthority()
                                                .equals("ROLE_SUPER_ADMIN"));

                if (isSuperAdmin) {

                        // SUPER_ADMIN can see users from all organizations
                        users = userService.findAll();

                } else {

                        // ADMIN can see only users from their organization
                        if (currentUser.getOrganization() == null) {
                                throw new IllegalStateException(
                                                "Admin is not assigned to an organization");
                        }

                        Long organizationId = currentUser.getOrganization().getId();

                        users = userService.findAllByOrganizationId(
                                        organizationId);
                }

                List<UserResponse> responses = users.stream()
                                .map(this::toResponse)
                                .toList();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Users fetched successfully",
                                                responses));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<UserResponse>> getUserById(
                        @PathVariable Long id,
                        Authentication authentication) {

                boolean isSuperAdmin = authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority -> authority.getAuthority()
                                                .equals("ROLE_SUPER_ADMIN"));

                User user;

                if (isSuperAdmin) {

                        // SUPER_ADMIN can access any user
                        user = userService.findById(id)
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "User not found with id: " + id));

                } else {

                        // Get the currently logged-in user
                        User currentUser = userService
                                        .findByEmail(authentication.getName())
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "Logged-in user not found"));

                        if (currentUser.getOrganization() == null) {
                                throw new IllegalStateException(
                                                "Admin is not assigned to an organization");
                        }

                        Long organizationId = currentUser.getOrganization().getId();

                        // User must belong to the same organization
                        user = userService
                                        .findByIdAndOrganizationId(id, organizationId)
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "User not found with id: " + id));
                }

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "User fetched successfully",
                                                toResponse(user)));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<UserResponse>> updateUser(
                        @PathVariable Long id,
                        @Valid @RequestBody UserUpdateRequest request,
                        Authentication authentication) {

                boolean isSuperAdmin = authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority -> authority.getAuthority()
                                                .equals("ROLE_SUPER_ADMIN"));

                User user;

                if (isSuperAdmin) {

                        // SUPER_ADMIN can update any user
                        user = userService.findById(id)
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "User not found with id: " + id));

                } else {

                        // Get logged-in ADMIN
                        User currentUser = userService
                                        .findByEmail(authentication.getName())
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "Logged-in user not found"));

                        if (currentUser.getOrganization() == null) {
                                throw new IllegalStateException(
                                                "Admin is not assigned to an organization");
                        }

                        Long organizationId = currentUser.getOrganization().getId();

                        // User must belong to the same organization
                        user = userService
                                        .findByIdAndOrganizationId(id, organizationId)
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "User not found with id: " + id));
                }

                // Keep your existing update assignments here
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setPhone(request.getPhone());

                if (request.getRole() != null &&
                                !request.getRole().isBlank()) {

                        if (!isSuperAdmin &&
                                        "SUPER_ADMIN".equalsIgnoreCase(request.getRole())) {

                                return ResponseEntity
                                                .status(HttpStatus.FORBIDDEN)
                                                .body(
                                                                ApiResponse.failure(
                                                                                "You cannot assign the SUPER_ADMIN role"));
                        }

                        Role role = roleService
                                        .findByName(request.getRole())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Role not found: " + request.getRole()));

                        user.setRole(role);
                }

                User updatedUser = userService.update(user);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "User updated successfully",
                                                toResponse(updatedUser)));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deactivateUser(
                        @PathVariable Long id,
                        Authentication authentication) {

                boolean isSuperAdmin = authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority -> authority.getAuthority()
                                                .equals("ROLE_SUPER_ADMIN"));

                User user;

                if (isSuperAdmin) {

                        user = userService.findById(id)
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "User not found with id: " + id));

                } else {

                        User currentUser = userService
                                        .findByEmail(authentication.getName())
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "Logged-in user not found"));

                        if (currentUser.getOrganization() == null) {
                                throw new IllegalStateException(
                                                "Admin is not assigned to an organization");
                        }

                        Long organizationId = currentUser.getOrganization().getId();

                        user = userService
                                        .findByIdAndOrganizationId(id, organizationId)
                                        .orElseThrow(() -> new UserNotFoundException(
                                                        "User not found with id: " + id));
                }

                user.setStatus(UserStatus.INACTIVE);

                userService.update(user);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "User deactivated successfully",
                                                null));
        }

        @GetMapping("/security-test")
        public String securityTest(
                        org.springframework.security.core.Authentication authentication) {

                return "User: " + authentication.getName()
                                + " | Authorities: " + authentication.getAuthorities();
        };

        private UserResponse toResponse(User user) {

                String roleName = user.getRole() != null
                                ? user.getRole().getName()
                                : null;

                return new UserResponse(
                                user.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getRole().getName(),
                                user.getStatus(),
                                user.getOrganization() != null
                                                ? user.getOrganization().getId()
                                                : null);
        }
}