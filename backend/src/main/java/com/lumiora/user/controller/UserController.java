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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;
        private final RoleService roleService;
        private final PasswordEncoder passwordEncoder;

        @PostMapping
        public ResponseEntity<?> createUser(
        @Valid @RequestBody UserCreateRequest request) {

        if (userService.existsByEmail(request.getEmail())) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                                .body(
                                ApiResponse.failure(
                                        "A user with this email already exists"
                                        )
                                );
        }

        Role role = roleService
                .findByName(request.getRole())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found: " + request.getRole()
                        ));

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // Never store the raw password
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userService.save(user);

        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(
                ApiResponse.success(
                        "User created successfully",
                        toResponse(savedUser)
                )
        );
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<UserResponse> users = userService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users fetched successfully",
                        users
                )
        );
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<UserResponse>> getUserById(
                @PathVariable Long id) {

        User user = userService.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully",
                        toResponse(user)
                )
        );
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<UserResponse>> updateUser(
                @PathVariable Long id,
                @RequestBody UserUpdateRequest request) {
        
            User user = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));
        
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone());
        
            if (request.getRole() != null &&
                    !request.getRole().isBlank()) {
        
                Role role = roleService
                        .findByName(request.getRole())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Role not found: " + request.getRole()
                                ));
        
                user.setRole(role);
            }
        
            User updatedUser = userService.update(user);
        
            return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully",
                        toResponse(updatedUser)
                )
        );
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deactivateUser(
                @PathVariable Long id) {
        
            User user = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));
        
            user.setStatus(UserStatus.INACTIVE);
        
            userService.update(user);
        
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "User deactivated successfully",
                            null
                    )
            );
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
            roleName,
            user.getStatus()
    );
}
}