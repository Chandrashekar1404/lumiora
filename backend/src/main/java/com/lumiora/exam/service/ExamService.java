package com.lumiora.exam.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.lumiora.exam.dto.ExamCreateRequest;
import com.lumiora.exam.dto.ExamResponse;
import com.lumiora.exam.dto.ExamResultCreateRequest;
import com.lumiora.exam.dto.ExamResultResponse;
import com.lumiora.exam.dto.ExamResultUpdateRequest;
import com.lumiora.exam.dto.ExamUpdateRequest;

public interface ExamService {

    ExamResponse createExam(
            ExamCreateRequest request,
            Authentication authentication
    );

    List<ExamResponse> getAllExams(
            Authentication authentication
    );

    ExamResponse getExamById(
            Long examId,
            Authentication authentication
    );

    List<ExamResponse> getExamsByBatch(
            Long batchId,
            Authentication authentication
    );

    List<ExamResponse> getMyExams(
            Authentication authentication
    );

    ExamResponse updateExam(
            Long examId,
            ExamUpdateRequest request,
            Authentication authentication
    );

    void deactivateExam(
            Long examId,
            Authentication authentication
    );

    ExamResultResponse createResult(
            Long examId,
            ExamResultCreateRequest request,
            Authentication authentication
    );

    List<ExamResultResponse> getExamResults(
            Long examId,
            Authentication authentication
    );

    List<ExamResultResponse> getMyResults(
            Authentication authentication
    );

    List<ExamResultResponse> getStudentResults(
            Long studentId,
            Authentication authentication
    );

    ExamResultResponse updateResult(
            Long resultId,
            ExamResultUpdateRequest request,
            Authentication authentication
    );

    void deactivateResult(
            Long resultId,
            Authentication authentication
    );
}