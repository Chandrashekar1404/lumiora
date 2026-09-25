package com.lumiora.exam.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.exam.entity.Exam;

public interface ExamRepository
        extends JpaRepository<Exam, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<Exam> findAll();

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    Optional<Exam> findById(Long id);

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<Exam> findAllByOrganization_Id(
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    Optional<Exam> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<Exam> findAllByOrganization_IdAndBatch_Id(
            Long organizationId,
            Long batchId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<Exam> findAllByOrganization_IdAndBatch_Trainer_Id(
            Long organizationId,
            Long trainerId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "batch.course",
            "batch.trainer"
    })
    List<Exam> findAllByOrganization_IdAndBatch_IdIn(
            Long organizationId,
            Collection<Long> batchIds
    );
}