package com.lumiora.assignment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lumiora.assignment.entity.AssignmentStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AssignmentUpdateRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 4000)
    private String description;

    private LocalDateTime dueDate;

    @DecimalMin(value = "0.01")
    private BigDecimal maxMarks;

    @Size(max = 1000)
    private String resourceUrl;

    private AssignmentStatus status;

    private Boolean active;
}