package com.lumiora.timetable.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.lumiora.batch.entity.BatchStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponse {

    private Long id;

    private DayOfWeek dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private String room;

    private String topic;

    private String notes;

    private boolean active;

    private Long organizationId;

    private Long batchId;

    private String batchName;

    private String batchCode;

    private BatchStatus batchStatus;

    private Long courseId;

    private String courseName;

    private Long trainerId;

    private String trainerName;
}