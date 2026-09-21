package com.lumiora.batch.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.lumiora.batch.entity.BatchStatus;

import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateRequest {

    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private String schedule;

    private LocalTime startTime;

    private LocalTime endTime;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Long courseId;

    private Long trainerId;

    private BatchStatus status;

    private Boolean active;
}