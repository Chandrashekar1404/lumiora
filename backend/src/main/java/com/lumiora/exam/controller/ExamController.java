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

import com.lumiora.exam.dto.ExamCreateRequest;
import com.lumiora.exam.dto.ExamResponse;
import com.lumiora.exam.dto.ExamUpdateRequest;
import com.lumiora.exam.service.ExamService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<ApiResponse<ExamResponse>>
            createExam(
                    @Valid @RequestBody
                    ExamCreateRequest request,
                    Authentication authentication
            ) {

        ExamResponse response =
                examService.createExam(
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Exam created successfully",
                                response
                        )
                );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<ExamResponse>>>
            getAllExams(
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Exams fetched successfully",
                        examService.getAllExams(
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // MY EXAMS
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<
            ApiResponse<List<ExamResponse>>>
            getMyExams(
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "My exams fetched successfully",
                        examService.getMyExams(
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // BATCH EXAMS
    // =========================================================

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<
            ApiResponse<List<ExamResponse>>>
            getExamsByBatch(
                    @PathVariable Long batchId,
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch exams fetched successfully",
                        examService.getExamsByBatch(
                                batchId,
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>>
            getExamById(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Exam fetched successfully",
                        examService.getExamById(
                                id,
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>>
            updateExam(
                    @PathVariable Long id,
                    @Valid @RequestBody
                    ExamUpdateRequest request,
                    Authentication authentication
            ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Exam updated successfully",
                        examService.updateExam(
                                id,
                                request,
                                authentication
                        )
                )
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deactivateExam(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        examService.deactivateExam(
                id,
                authentication
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Exam deactivated successfully",
                        null
                )
        );
    }
}