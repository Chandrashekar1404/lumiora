package com.lumiora.attendance.dto;

import java.time.LocalDate;

import com.lumiora.attendance.entity.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;

    private LocalDate attendanceDate;

    private AttendanceStatus status;

    private String remarks;

    private Long organizationId;

    private Long batchId;

    private String batchName;

    private String batchCode;

    private Long studentId;

    private String studentName;

    private String studentEmail;
}