package com.lumiora.assignment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AssignmentCreateRequest {

    @NotBlank(message = "Assignment title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 4000)
    private String description;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @NotNull(message = "Maximum marks are required")
    @DecimalMin(value = "0.01", message = "Maximum marks must be greater than zero")
    private BigDecimal maxMarks;

    @Size(max = 1000)
    private String resourceUrl;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;
}