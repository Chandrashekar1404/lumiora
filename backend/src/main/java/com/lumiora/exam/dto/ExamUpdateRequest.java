package com.lumiora.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lumiora.exam.entity.ExamStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamUpdateRequest {

    @Size(max = 200, message = "Exam name cannot exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private LocalDate examDate;

    @DecimalMin(
            value = "0.01",
            message = "Maximum marks must be greater than 0"
    )
    private BigDecimal maxMarks;

    private Long batchId;

    private ExamStatus status;

    private Boolean active;
}