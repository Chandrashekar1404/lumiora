package com.lumiora.assignment.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.lumiora.assignment.dto.AssignmentCreateRequest;
import com.lumiora.assignment.dto.AssignmentResponse;
import com.lumiora.assignment.dto.AssignmentSubmissionResponse;
import com.lumiora.assignment.dto.AssignmentUpdateRequest;
import com.lumiora.assignment.dto.SubmissionCreateRequest;
import com.lumiora.assignment.dto.SubmissionGradeRequest;
import com.lumiora.assignment.dto.SubmissionUpdateRequest;

public interface AssignmentService {

    AssignmentResponse createAssignment(
        AssignmentCreateRequest request,
        Authentication authentication
    );

    List<AssignmentResponse> getAllAssignments(
        Authentication authentication
    );

    List<AssignmentResponse> getMyAssignments(
        Authentication authentication
    );

    AssignmentResponse getAssignmentById(
        Long assignmentId,
        Authentication authentication
    );

    List<AssignmentResponse> getAssignmentsByBatch(
        Long batchId,
        Authentication authentication
    );

    AssignmentResponse updateAssignment(
        Long assignmentId,
        AssignmentUpdateRequest request,
        Authentication authentication
    );

    void deactivateAssignment(
        Long assignmentId,
        Authentication authentication
    );

    AssignmentSubmissionResponse createSubmission(
        Long assignmentId,
        SubmissionCreateRequest request,
        Authentication authentication
    );

    List<AssignmentSubmissionResponse> getMySubmissions(
        Authentication authentication
    );

    List<AssignmentSubmissionResponse> getSubmissionsForAssignment(
        Long assignmentId,
        Authentication authentication
    );

    AssignmentSubmissionResponse resubmit(
        Long submissionId,
        SubmissionUpdateRequest request,
        Authentication authentication
    );

    AssignmentSubmissionResponse gradeSubmission(
        Long submissionId,
        SubmissionGradeRequest request,
        Authentication authentication
    );
}