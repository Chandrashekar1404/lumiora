package com.lumiora.assignment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.assignment.entity.Assignment;

public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    @Override
    @EntityGraph(attributePaths = {
        "organization",
        "batch",
        "batch.course",
        "batch.trainer",
        "createdBy"
    })
    List<Assignment> findAll();

    @Override
    @EntityGraph(attributePaths = {
        "organization",
        "batch",
        "batch.course",
        "batch.trainer",
        "createdBy"
    })
    Optional<Assignment> findById(Long id);

    @EntityGraph(attributePaths = {
        "organization",
        "batch",
        "batch.course",
        "batch.trainer",
        "createdBy"
    })
    List<Assignment> findAllByOrganization_Id(
        Long organizationId
    );

    @EntityGraph(attributePaths = {
        "organization",
        "batch",
        "batch.course",
        "batch.trainer",
        "createdBy"
    })
    List<Assignment> findAllByOrganization_IdAndBatch_Id(
        Long organizationId,
        Long batchId
    );

    @EntityGraph(attributePaths = {
        "organization",
        "batch",
        "batch.course",
        "batch.trainer",
        "createdBy"
    })
    List<Assignment> findAllByOrganization_IdAndBatch_Trainer_Id(
        Long organizationId,
        Long trainerId
    );

    @EntityGraph(attributePaths = {
        "organization",
        "batch",
        "batch.course",
        "batch.trainer",
        "createdBy"
    })
    List<Assignment> findAllByOrganization_IdAndBatch_IdIn(
        Long organizationId,
        Collection<Long> batchIds
    );
}