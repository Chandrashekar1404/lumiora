package com.lumiora.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.assignment.entity.AssignmentSubmission;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    @Override
    @EntityGraph(attributePaths = {
        "assignment",
        "assignment.organization",
        "assignment.batch",
        "assignment.batch.course",
        "assignment.batch.trainer",
        "student",
        "organization",
        "gradedBy"
    })
    List<AssignmentSubmission> findAll();

    @Override
    @EntityGraph(attributePaths = {
        "assignment",
        "assignment.organization",
        "assignment.batch",
        "assignment.batch.course",
        "assignment.batch.trainer",
        "student",
        "organization",
        "gradedBy"
    })
    Optional<AssignmentSubmission> findById(Long id);

    @EntityGraph(attributePaths = {
        "assignment",
        "assignment.organization",
        "assignment.batch",
        "assignment.batch.course",
        "assignment.batch.trainer",
        "student",
        "organization",
        "gradedBy"
    })
    Optional<AssignmentSubmission> findByIdAndActiveTrue(
        Long id
    );

    @EntityGraph(attributePaths = {
        "assignment",
        "assignment.organization",
        "assignment.batch",
        "assignment.batch.course",
        "assignment.batch.trainer",
        "student",
        "organization",
        "gradedBy"
    })
    Optional<AssignmentSubmission> findByAssignment_IdAndStudent_IdAndActiveTrue(
        Long assignmentId,
        Long studentId
    );

    @EntityGraph(attributePaths = {
        "assignment",
        "assignment.organization",
        "assignment.batch",
        "assignment.batch.course",
        "assignment.batch.trainer",
        "student",
        "organization",
        "gradedBy"
    })
    List<AssignmentSubmission> findAllByOrganization_IdAndStudent_IdAndActiveTrueOrderBySubmittedAtDesc(
        Long organizationId,
        Long studentId
    );

    @EntityGraph(attributePaths = {
        "assignment",
        "assignment.organization",
        "assignment.batch",
        "assignment.batch.course",
        "assignment.batch.trainer",
        "student",
        "organization",
        "gradedBy"
    })
    List<AssignmentSubmission> findAllByAssignment_IdAndOrganization_IdAndActiveTrueOrderBySubmittedAtDesc(
        Long assignmentId,
        Long organizationId
    );

    @EntityGraph(attributePaths = {
        "assignment",
        "assignment.organization",
        "assignment.batch",
        "assignment.batch.course",
        "assignment.batch.trainer",
        "student",
        "organization",
        "gradedBy"
    })
    List<AssignmentSubmission> findAllByAssignment_IdAndActiveTrueOrderBySubmittedAtDesc(
        Long assignmentId
    );
}