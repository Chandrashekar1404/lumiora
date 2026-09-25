package com.lumiora.timetable.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.timetable.entity.TimetableEntry;

public interface TimetableRepository
        extends JpaRepository<TimetableEntry, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<TimetableEntry> findAll();

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    Optional<TimetableEntry> findById(Long id);

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<TimetableEntry> findAllByOrganization_Id(
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    Optional<TimetableEntry> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<TimetableEntry> findAllByOrganization_IdAndBatch_Id(
            Long organizationId,
            Long batchId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<TimetableEntry> findAllByOrganization_IdAndBatch_Trainer_IdAndActiveTrue(
            Long organizationId,
            Long trainerId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<TimetableEntry> findAllByOrganization_IdAndBatch_IdInAndActiveTrue(
            Long organizationId,
            Collection<Long> batchIds
    );
}