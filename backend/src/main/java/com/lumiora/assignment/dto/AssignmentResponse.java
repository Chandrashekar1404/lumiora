package com.lumiora.assignment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lumiora.assignment.entity.AssignmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;

    private String title;

    private String description;

    private LocalDateTime dueDate;

    private BigDecimal maxMarks;

    private String resourceUrl;

    private AssignmentStatus status;

    private boolean active;

    private Long organizationId;

    private Long batchId;

    private String batchName;

    private String courseName;

    private Long trainerId;

    private String trainerName;

    private Long createdById;

    private String createdByName;
}