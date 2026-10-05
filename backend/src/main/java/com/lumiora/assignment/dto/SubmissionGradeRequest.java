package com.lumiora.assignment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class SubmissionGradeRequest {

    @NotNull(message = "Marks awarded are required")
    @DecimalMin(value = "0.00", message = "Marks cannot be negative")
    private BigDecimal marksAwarded;

    @Size(max = 2000)
    private String feedback;
}