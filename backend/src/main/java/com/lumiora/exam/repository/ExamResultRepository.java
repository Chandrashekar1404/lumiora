package com.lumiora.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.exam.entity.ExamResult;

public interface ExamResultRepository
        extends JpaRepository<ExamResult, Long> {

    @EntityGraph(attributePaths = {
            "exam",
            "exam.organization",
            "exam.batch",
            "exam.batch.course",
            "exam.batch.trainer",
            "student",
            "organization"
    })
    List<ExamResult> findAll();

    @EntityGraph(attributePaths = {
            "exam",
            "exam.organization",
            "exam.batch",
            "exam.batch.course",
            "exam.batch.trainer",
            "student",
            "organization"
    })
    Optional<ExamResult> findById(Long id);

    @EntityGraph(attributePaths = {
            "exam",
            "exam.organization",
            "exam.batch",
            "exam.batch.course",
            "exam.batch.trainer",
            "student",
            "organization"
    })
    Optional<ExamResult> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "exam",
            "exam.organization",
            "exam.batch",
            "exam.batch.course",
            "exam.batch.trainer",
            "student",
            "organization"
    })
    List<ExamResult> findAllByExam_Id(
            Long examId
    );

    @EntityGraph(attributePaths = {
            "exam",
            "exam.organization",
            "exam.batch",
            "exam.batch.course",
            "exam.batch.trainer",
            "student",
            "organization"
    })
    List<ExamResult> findAllByExam_IdAndOrganization_Id(
            Long examId,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "exam",
            "exam.organization",
            "exam.batch",
            "exam.batch.course",
            "exam.batch.trainer",
            "student",
            "organization"
    })
    List<ExamResult> findAllByStudent_IdAndOrganization_IdOrderByExam_ExamDateDesc(
            Long studentId,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "exam",
            "exam.organization",
            "exam.batch",
            "exam.batch.course",
            "exam.batch.trainer",
            "student",
            "organization"
    })
    Optional<ExamResult> findByExam_IdAndStudent_Id(
            Long examId,
            Long studentId
    );
}