package com.lumiora.assignment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lumiora.assignment.entity.SubmissionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionResponse {

    private Long id;

    private Long assignmentId;

    private String assignmentTitle;

    private Long studentId;

    private String studentName;

    private String studentEmail;

    private String submissionText;

    private String fileUrl;

    private LocalDateTime submittedAt;

    private SubmissionStatus status;

    private BigDecimal marksAwarded;

    private String feedback;

    private LocalDateTime gradedAt;

    private Long gradedById;

    private String gradedByName;
}