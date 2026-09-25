package com.lumiora.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lumiora.exam.entity.ExamStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDate examDate;

    private BigDecimal maxMarks;

    private ExamStatus status;

    private boolean active;

    private Long organizationId;

    private Long batchId;

    private String batchName;

    private String batchCode;

    private Long courseId;

    private String courseName;

    private Long trainerId;

    private String trainerName;
}