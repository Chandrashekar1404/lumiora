package com.lumiora.exam.entity;

import java.math.BigDecimal;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.base.BaseEntity;
import com.lumiora.organization.entity.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "exam_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_result_exam_student",
                        columnNames = {
                                "exam_id",
                                "student_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ExamResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal marks;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal percentage;

    @Column(nullable = false, length = 5)
    private String grade;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private boolean active = true;
}