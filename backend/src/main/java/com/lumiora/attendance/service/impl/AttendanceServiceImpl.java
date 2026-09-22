package com.lumiora.attendance.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.attendance.entity.Attendance;
import com.lumiora.attendance.repository.AttendanceRepository;
import com.lumiora.attendance.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl
        implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Override
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    @Override
    public Optional<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    @Override
    public List<Attendance> findAllByOrganizationId(
            Long organizationId) {

        return attendanceRepository
                .findAllByOrganization_Id(
                        organizationId);
    }

    @Override
    public Optional<Attendance> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return attendanceRepository
                .findByIdAndOrganization_Id(
                        id,
                        organizationId);
    }

    @Override
    public boolean existsByStudentAndBatchAndDate(
            Long studentId,
            Long batchId,
            LocalDate attendanceDate) {

        return attendanceRepository
                .existsByStudent_IdAndBatch_IdAndAttendanceDate(
                        studentId,
                        batchId,
                        attendanceDate);
    }

    @Override
    public List<Attendance> findAllByOrganizationAndBatch(
            Long organizationId,
            Long batchId) {

        return attendanceRepository
                .findAllByOrganization_IdAndBatch_Id(
                        organizationId,
                        batchId);
    }

    @Override
    public List<Attendance> findAllByOrganizationAndDate(
            Long organizationId,
            LocalDate attendanceDate) {

        return attendanceRepository
                .findAllByOrganization_IdAndAttendanceDate(
                        organizationId,
                        attendanceDate);
    }

    public interface AttendanceService {

        Attendance save(Attendance attendance);

        List<Attendance> findAll();

        Optional<Attendance> findById(Long id);

        List<Attendance> findAllByOrganizationId(
                Long organizationId);

        Optional<Attendance> findByIdAndOrganizationId(
                Long id,
                Long organizationId);

        boolean existsByStudentAndBatchAndDate(
                Long studentId,
                Long batchId,
                LocalDate attendanceDate);

        List<Attendance> findAllByOrganizationAndBatch(
                Long organizationId,
                Long batchId);

        List<Attendance> findAllByOrganizationAndDate(
                Long organizationId,
                LocalDate attendanceDate);
    }

    @Override
    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }
}