package com.lumiora.assignment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.assignment.dto.AssignmentSubmissionResponse;
import com.lumiora.assignment.dto.SubmissionCreateRequest;
import com.lumiora.assignment.dto.SubmissionGradeRequest;
import com.lumiora.assignment.dto.SubmissionUpdateRequest;
import com.lumiora.assignment.service.AssignmentService;
import com.lumiora.dto.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assignment-submissions")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

    private final AssignmentService assignmentService;

    @PostMapping("/assignment/{assignmentId}")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionCreateRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                "Assignment submitted successfully",
                assignmentService.createSubmission(
                    assignmentId,
                    request,
                    authentication
                )
            )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionResponse>>> getMySubmissions(
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "My submissions fetched successfully",
                assignmentService.getMySubmissions(authentication)
            )
        );
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionResponse>>> getSubmissionsForAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "Assignment submissions fetched successfully",
                assignmentService.getSubmissionsForAssignment(
                    assignmentId,
                    authentication
                )
            )
        );
    }

    @PutMapping("/{submissionId}/resubmit")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> resubmit(
            @PathVariable Long submissionId,
            @Valid @RequestBody SubmissionUpdateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "Assignment resubmitted successfully",
                assignmentService.resubmit(
                    submissionId,
                    request,
                    authentication
                )
            )
        );
    }

    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody SubmissionGradeRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
            ApiResponse.success(
                "Submission graded successfully",
                assignmentService.gradeSubmission(
                    submissionId,
                    request,
                    authentication
                )
            )
        );
    }
}