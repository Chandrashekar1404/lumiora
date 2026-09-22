package com.lumiora.enrollment.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.repository.EnrollmentRepository;
import com.lumiora.enrollment.service.EnrollmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl
        implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Enrollment save(Enrollment enrollment) {
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepository.findById(id);
    }

    @Override
    public List<Enrollment> findAllByOrganizationId(
            Long organizationId) {

        return enrollmentRepository
                .findAllByOrganization_Id(organizationId);
    }

    @Override
    public Optional<Enrollment> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return enrollmentRepository
                .findByIdAndOrganization_Id(
                        id,
                        organizationId
                );
    }

    @Override
    public boolean existsByStudentAndBatch(
            Long studentId,
            Long batchId) {

        return enrollmentRepository
                .existsByStudent_IdAndBatch_Id(
                        studentId,
                        batchId
                );
    }

    @Override
    public long countByBatchAndStatus(
            Long batchId,
            EnrollmentStatus status) {

        return enrollmentRepository
                .countByBatch_IdAndStatus(
                        batchId,
                        status
                );
    }
}