package com.lumiora.certificate.entity;

import java.time.LocalDate;

import com.lumiora.batch.entity.Batch;
import com.lumiora.course.entity.Course;
import com.lumiora.entity.auth.User;
import com.lumiora.entity.base.BaseEntity;
import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.organization.entity.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_certificate_number",
                        columnNames = "certificate_number"
                ),
                @UniqueConstraint(
                        name = "uk_certificate_enrollment",
                        columnNames = "enrollment_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Certificate extends BaseEntity {

    @Column(
            name = "certificate_number",
            nullable = false,
            unique = true,
            length = 100
    )
    private String certificateNumber;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(length = 500)
    private String achievement;

    @Column(length = 1000)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CertificateStatus status = CertificateStatus.ISSUED;

    @Column(nullable = false)
    private boolean active = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "enrollment_id",
            nullable = false
    )
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "course_id",
            nullable = false
    )
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "batch_id",
            nullable = false
    )
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "issued_by_id"
    )
    private User issuedBy;
}