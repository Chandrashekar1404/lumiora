package com.lumiora.student.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.attendance.entity.Attendance;
import com.lumiora.attendance.repository.AttendanceRepository;
import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.repository.EnrollmentRepository;
import com.lumiora.entity.auth.User;
import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.repository.FeeAccountRepository;
import com.lumiora.repository.UserRepository;
import com.lumiora.student.service.StudentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeeAccountRepository feeAccountRepository;

    @Override
    public List<User> findAllStudents() {
        return userRepository.findAllByRole_Name("STUDENT");
    }

    @Override
    public List<User> findAllStudentsByOrganization(
            Long organizationId
    ) {
        return userRepository
                .findAllByOrganization_IdAndRole_Name(
                        organizationId,
                        "STUDENT"
                );
    }

    @Override
    public Optional<User> findStudentById(Long id) {

        return userRepository
                .findById(id)
                .filter(this::isStudent);
    }

    @Override
    public Optional<User> findStudentByIdAndOrganization(
            Long id,
            Long organizationId
    ) {

        return userRepository
                .findByIdAndOrganization_Id(id, organizationId)
                .filter(this::isStudent);
    }

    @Override
    public List<Enrollment> findStudentEnrollments(
            Long organizationId,
            Long studentId
    ) {

        return enrollmentRepository
                .findAllByOrganization_IdAndStudent_Id(
                        organizationId,
                        studentId
                );
    }

    @Override
    public List<Attendance> findStudentAttendance(
            Long organizationId,
            Long studentId
    ) {

        return attendanceRepository
                .findAllByOrganization_IdAndStudent_Id(
                        organizationId,
                        studentId
                );
    }

    @Override
    public List<FeeAccount> findStudentFees(
            Long organizationId,
            Long studentId
    ) {

        return feeAccountRepository
                .findAllByOrganization_IdAndEnrollment_Student_Id(
                        organizationId,
                        studentId
                );
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public User save(User student) {
        return userRepository.save(student);
    }

    private boolean isStudent(User user) {

        return user.getRole() != null
                && "STUDENT".equals(
                        user.getRole().getName()
                );
    }
}