package com.lumiora.batch.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.lumiora.batch.entity.BatchStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {

    private Long id;

    private String name;

    private String code;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private String schedule;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;

    private BatchStatus status;

    private boolean active;

    private Long organizationId;

    private Long courseId;

    private String courseName;

    private Long trainerId;

    private String trainerName;
}