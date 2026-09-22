package com.lumiora.attendance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.lumiora.attendance.entity.Attendance;

public interface AttendanceService {

    Attendance save(Attendance attendance);

    List<Attendance> findAll();

    Optional<Attendance> findById(Long id);

    List<Attendance> findAllByOrganizationId(
            Long organizationId
    );

    Optional<Attendance> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByStudentAndBatchAndDate(
            Long studentId,
            Long batchId,
            LocalDate attendanceDate
    );

    List<Attendance> findAllByOrganizationAndBatch(
            Long organizationId,
            Long batchId
    );

    List<Attendance> findAllByOrganizationAndDate(
            Long organizationId,
            LocalDate attendanceDate
    );

    void delete(Long id);
}