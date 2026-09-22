package com.lumiora.lead.dto;

import java.time.LocalDate;

import com.lumiora.lead.entity.LeadSource;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadCreateRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String notes;

    @FutureOrPresent(
            message = "Follow-up date cannot be in the past"
    )
    private LocalDate followUpDate;

    @NotNull(message = "Lead source is required")
    private LeadSource source;

    private Long courseId;

    private Long counselorId;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;
}