
package com.lumiora.lead.dto;

import java.time.LocalDate;

import com.lumiora.lead.entity.LeadSource;
import com.lumiora.lead.entity.LeadStatus;

import jakarta.validation.constraints.FutureOrPresent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadUpdateRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String notes;

    @FutureOrPresent(
            message = "Follow-up date cannot be in the past"
    )
    private LocalDate followUpDate;

    private LeadSource source;

    private LeadStatus status;

    private Long courseId;

    private Long counselorId;
}