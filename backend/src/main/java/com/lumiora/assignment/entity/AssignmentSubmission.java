package com.lumiora.assignment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.base.BaseEntity;
import com.lumiora.organization.entity.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "assignment_submissions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_assignment_submission_student",
            columnNames = {"assignment_id", "student_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class AssignmentSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(length = 5000)
    private String submissionText;

    @Column(length = 1000)
    private String fileUrl;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(precision = 8, scale = 2)
    private BigDecimal marksAwarded;

    @Column(length = 2000)
    private String feedback;

    private LocalDateTime gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by_id")
    private User gradedBy;

    @Column(nullable = false)
    private boolean active = true;
}