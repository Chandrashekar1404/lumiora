package com.lumiora.exam.controller;

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

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.exam.dto.ExamResultCreateRequest;
import com.lumiora.exam.dto.ExamResultResponse;
import com.lumiora.exam.dto.ExamResultUpdateRequest;
import com.lumiora.exam.service.ExamService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ExamResultController {

    private final ExamService examService;


    // =========================================================
    // CREATE RESULT
    // =========================================================

    @PostMapping("/exam/{examId}")
    public ResponseEntity<
            ApiResponse<ExamResultResponse>>
            createResult(
                    @PathVariable Long examId,
                    @Valid @RequestBody
                    ExamResultCreateRequest request,
                    Authentication authentication
            ) {

        ExamResultResponse response =
                examService.createResult(
                        examId,
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Exam result created successfully",
                                response
                        )
                );
    }


    // =========================================================
    // MY RESULTS
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<
            ApiResponse<List<ExamResultResponse>>>
            getMyResults(
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "My exam results fetched successfully",
                        examService.getMyResults(
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // STUDENT RESULTS
    // =========================================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<
            ApiResponse<List<ExamResultResponse>>>
            getStudentResults(
                    @PathVariable Long studentId,
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student exam results fetched successfully",
                        examService.getStudentResults(
                                studentId,
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // RESULTS FOR ONE EXAM
    // =========================================================

    @GetMapping("/exam/{examId}")
    public ResponseEntity<
            ApiResponse<List<ExamResultResponse>>>
            getExamResults(
                    @PathVariable Long examId,
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Exam results fetched successfully",
                        examService.getExamResults(
                                examId,
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // UPDATE RESULT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<
            ApiResponse<ExamResultResponse>>
            updateResult(
                    @PathVariable Long id,
                    @Valid @RequestBody
                    ExamResultUpdateRequest request,
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Exam result updated successfully",
                        examService.updateResult(
                                id,
                                request,
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // DEACTIVATE RESULT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deactivateResult(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        examService.deactivateResult(
                id,
                authentication
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Exam result deactivated successfully",
                        null
                )
        );
    }
}