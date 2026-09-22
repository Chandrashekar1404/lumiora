package com.lumiora.attendance.dto;

import com.lumiora.attendance.entity.AttendanceStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequest {

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    private String remarks;
}