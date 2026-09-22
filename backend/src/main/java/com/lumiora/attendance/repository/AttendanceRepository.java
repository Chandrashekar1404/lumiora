package com.lumiora.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.attendance.entity.Attendance;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    List<Attendance> findAll();

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    Optional<Attendance> findById(Long id);

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    List<Attendance> findAllByOrganization_Id(
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    Optional<Attendance> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    boolean existsByStudent_IdAndBatch_IdAndAttendanceDate(
            Long studentId,
            Long batchId,
            LocalDate attendanceDate
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    List<Attendance> findAllByOrganization_IdAndBatch_Id(
            Long organizationId,
            Long batchId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "batch",
            "student"
    })
    List<Attendance> findAllByOrganization_IdAndAttendanceDate(
            Long organizationId,
            LocalDate attendanceDate
    );
}