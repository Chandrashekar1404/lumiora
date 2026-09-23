package com.lumiora.student.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.attendance.entity.Attendance;
import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.entity.auth.User;
import com.lumiora.fee.entity.FeeAccount;

public interface StudentService {

    List<User> findAllStudents();

    List<User> findAllStudentsByOrganization(Long organizationId);

    Optional<User> findStudentById(Long id);

    Optional<User> findStudentByIdAndOrganization(
            Long id,
            Long organizationId
    );

    List<Enrollment> findStudentEnrollments(
            Long organizationId,
            Long studentId
    );

    List<Attendance> findStudentAttendance(
            Long organizationId,
            Long studentId
    );

    List<FeeAccount> findStudentFees(
            Long organizationId,
            Long studentId
    );

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);

    User save(User student);
}