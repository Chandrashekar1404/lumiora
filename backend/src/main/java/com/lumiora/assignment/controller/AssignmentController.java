package com.lumiora.assignment.controller;

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

import com.lumiora.assignment.dto.AssignmentCreateRequest;
import com.lumiora.assignment.dto.AssignmentResponse;
import com.lumiora.assignment.dto.AssignmentUpdateRequest;
import com.lumiora.assignment.service.AssignmentService;
import com.lumiora.dto.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody AssignmentCreateRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Assignment created successfully",
                assignmentService.createAssignment(request, authentication)
            )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAllAssignments(
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "Assignments fetched successfully",
                assignmentService.getAllAssignments(authentication)
            )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getMyAssignments(
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "My assignments fetched successfully",
                assignmentService.getMyAssignments(authentication)
            )
        );
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignmentsByBatch(
            @PathVariable Long batchId,
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "Batch assignments fetched successfully",
                assignmentService.getAssignmentsByBatch(
                    batchId,
                    authentication
                )
            )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "Assignment fetched successfully",
                assignmentService.getAssignmentById(id, authentication)
            )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "Assignment updated successfully",
                assignmentService.updateAssignment(
                    id,
                    request,
                    authentication
                )
            )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateAssignment(
            @PathVariable Long id,
            Authentication authentication) {

        assignmentService.deactivateAssignment(id, authentication);

        return ResponseEntity.ok(
            ApiResponse.success(
                "Assignment deactivated successfully",
                null
            )
        );
    }
}