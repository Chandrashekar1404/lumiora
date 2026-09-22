package com.lumiora.lead.dto;

import java.time.LocalDate;

import com.lumiora.lead.entity.LeadSource;
import com.lumiora.lead.entity.LeadStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String notes;

    private LocalDate followUpDate;

    private LeadStatus status;

    private LeadSource source;

    private boolean active;

    private Long organizationId;

    private Long courseId;

    private String courseName;

    private Long counselorId;

    private String counselorName;
}