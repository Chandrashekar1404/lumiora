package com.lumiora.assignment.dto;

import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class SubmissionUpdateRequest {

    @Size(max = 5000)
    private String submissionText;

    @Size(max = 1000)
    private String fileUrl;
}