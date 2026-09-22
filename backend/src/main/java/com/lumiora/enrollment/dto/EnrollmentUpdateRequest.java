package com.lumiora.enrollment.dto;

import com.lumiora.enrollment.entity.EnrollmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentUpdateRequest {

    private EnrollmentStatus status;

    private String notes;
}