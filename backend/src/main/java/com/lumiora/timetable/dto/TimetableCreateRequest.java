package com.lumiora.timetable.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableCreateRequest {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Size(max = 100, message = "Room cannot exceed 100 characters")
    private String room;

    @Size(max = 200, message = "Topic cannot exceed 200 characters")
    private String topic;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}