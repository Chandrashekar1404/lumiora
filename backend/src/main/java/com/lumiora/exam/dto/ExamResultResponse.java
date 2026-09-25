package com.lumiora.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultResponse {

    private Long id;

    private Long examId;

    private String examName;

    private LocalDate examDate;

    private BigDecimal maxMarks;

    private Long studentId;

    private String studentName;

    private String studentEmail;

    private BigDecimal marks;

    private BigDecimal percentage;

    private String grade;

    private String remarks;

    private boolean active;

    private Long organizationId;

    private Long batchId;

    private String batchName;

    private Long courseId;

    private String courseName;
}