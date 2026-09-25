package com.lumiora.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreateRequest {

    @NotBlank(message = "Exam name is required")
    @Size(max = 200, message = "Exam name cannot exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Maximum marks are required")
    @DecimalMin(
            value = "0.01",
            message = "Maximum marks must be greater than 0"
    )
    private BigDecimal maxMarks;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;
}