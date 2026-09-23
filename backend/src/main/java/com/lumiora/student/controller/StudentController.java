package com.lumiora.student.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.attendance.dto.AttendanceResponse;
import com.lumiora.attendance.entity.Attendance;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.enrollment.dto.EnrollmentResponse;
import com.lumiora.enrollment.entity.Enrollment;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.fee.dto.FeeResponse;
import com.lumiora.fee.entity.FeeAccount;

import com.lumiora.service.UserService;

import com.lumiora.student.dto.StudentResponse;
import com.lumiora.student.dto.StudentUpdateRequest;
import com.lumiora.student.service.StudentService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    private final UserService userService;


    // =========================================================
    // GET ALL STUDENTS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>>
            getAllStudents(
                    Authentication authentication
            ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();

        List<User> students;

        if ("SUPER_ADMIN".equals(role)) {

            students =
                    studentService.findAllStudents();

        } else if ("ADMIN".equals(role)) {

            validateOrganization(currentUser);

            students =
                    studentService
                            .findAllStudentsByOrganization(
                                    currentUser
                                            .getOrganization()
                                            .getId()
                            );

        } else if ("STUDENT".equals(role)) {

            students = List.of(currentUser);

        } else {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ApiResponse.failure(
                                    "You are not authorized to view students"
                            )
                    );
        }

        List<StudentResponse> responses =
                students.stream()
                        .map(this::toStudentResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Students fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET MY PROFILE
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentResponse>>
            getMyProfile(
                    Authentication authentication
            ) {

        User currentUser =
                getCurrentUser(authentication);

        if (!isStudent(currentUser)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            ApiResponse.failure(
                                    "Only students can access this endpoint"
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student profile fetched successfully",
                        toStudentResponse(currentUser)
                )
        );
    }


    // =========================================================
    // GET STUDENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>>
            getStudentById(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User student =
                getAccessibleStudent(
                        id,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student fetched successfully",
                        toStudentResponse(student)
                )
        );
    }


    // =========================================================
    // GET STUDENT ENROLLMENTS
    // =========================================================

    @GetMapping("/{id}/enrollments")
    public ResponseEntity<
            ApiResponse<List<EnrollmentResponse>>>
            getStudentEnrollments(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User student =
                getAccessibleStudent(
                        id,
                        authentication
                );

        if (student.getOrganization() == null) {

            throw new IllegalStateException(
                    "Student is not assigned to an organization"
            );
        }

        List<Enrollment> enrollments =
                studentService.findStudentEnrollments(
                        student.getOrganization().getId(),
                        student.getId()
                );

        List<EnrollmentResponse> responses =
                enrollments.stream()
                        .map(this::toEnrollmentResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student enrollments fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET STUDENT ATTENDANCE
    // =========================================================

    @GetMapping("/{id}/attendance")
    public ResponseEntity<
            ApiResponse<List<AttendanceResponse>>>
            getStudentAttendance(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User student =
                getAccessibleStudent(
                        id,
                        authentication
                );

        if (student.getOrganization() == null) {

            throw new IllegalStateException(
                    "Student is not assigned to an organization"
            );
        }

        List<Attendance> attendance =
                studentService.findStudentAttendance(
                        student.getOrganization().getId(),
                        student.getId()
                );

        List<AttendanceResponse> responses =
                attendance.stream()
                        .map(this::toAttendanceResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student attendance fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET STUDENT FEES
    // =========================================================

    @GetMapping("/{id}/fees")
    public ResponseEntity<
            ApiResponse<List<FeeResponse>>>
            getStudentFees(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        User student =
                getAccessibleStudent(
                        id,
                        authentication
                );

        if (student.getOrganization() == null) {

            throw new IllegalStateException(
                    "Student is not assigned to an organization"
            );
        }

        List<FeeAccount> fees =
                studentService.findStudentFees(
                        student.getOrganization().getId(),
                        student.getId()
                );

        List<FeeResponse> responses =
                fees.stream()
                        .map(this::toFeeResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student fees fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // UPDATE STUDENT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>>
            updateStudent(
                    @PathVariable Long id,
                    @Valid @RequestBody StudentUpdateRequest request,
                    Authentication authentication
            ) {

        User student =
                getAccessibleStudent(
                        id,
                        authentication
                );

        /*
         * Phone can be changed, but it must remain unique.
         */
        if (!student.getPhone().equals(request.getPhone())) {

            if (studentService
                    .existsByPhone(request.getPhone())) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                                ApiResponse.failure(
                                        "A user with this phone number already exists"
                                )
                        );
            }
        }

        student.setFirstName(
                request.getFirstName()
        );

        student.setLastName(
                request.getLastName()
        );

        student.setPhone(
                request.getPhone()
        );

        User updatedStudent =
                studentService.save(student);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student updated successfully",
                        toStudentResponse(updatedStudent)
                )
        );
    }


    // =========================================================
    // ACCESS CONTROL
    // =========================================================

    private User getAccessibleStudent(
            Long studentId,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();


        // SUPER ADMIN
        if ("SUPER_ADMIN".equals(role)) {

            return studentService
                    .findStudentById(studentId)
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Student not found with id: "
                                            + studentId
                            ));
        }


        // ADMIN
        if ("ADMIN".equals(role)) {

            validateOrganization(currentUser);

            return studentService
                    .findStudentByIdAndOrganization(
                            studentId,
                            currentUser
                                    .getOrganization()
                                    .getId()
                    )
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Student not found with id: "
                                            + studentId
                            ));
        }


        // STUDENT
        if ("STUDENT".equals(role)) {

            if (!currentUser
                    .getId()
                    .equals(studentId)) {

                throw new IllegalArgumentException(
                        "Students can access only their own information"
                );
            }

            return currentUser;
        }


        throw new IllegalArgumentException(
                "You are not authorized to access student information"
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser(
            Authentication authentication
    ) {

        return userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Logged-in user not found"
                        ));
    }


    // =========================================================
    // ORGANIZATION VALIDATION
    // =========================================================

    private void validateOrganization(
            User user
    ) {

        if (user.getOrganization() == null) {

            throw new IllegalStateException(
                    "User is not assigned to an organization"
            );
        }
    }


    // =========================================================
    // STUDENT CHECK
    // =========================================================

    private boolean isStudent(
            User user
    ) {

        return user.getRole() != null
                && "STUDENT".equals(
                        user.getRole().getName()
                );
    }


    // =========================================================
    // STUDENT RESPONSE
    // =========================================================

    private StudentResponse toStudentResponse(
            User student
    ) {

        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                student.getStatus(),
                student.getOrganization() != null
                        ? student.getOrganization().getId()
                        : null
        );
    }


    // =========================================================
    // ENROLLMENT RESPONSE
    // =========================================================

    private EnrollmentResponse toEnrollmentResponse(
            Enrollment enrollment
    ) {

        String studentName =
                enrollment.getStudent()
                        .getFirstName()
                        + " "
                        + (
                        enrollment.getStudent()
                                .getLastName() != null
                                ? enrollment.getStudent()
                                .getLastName()
                                : ""
                );

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrollmentDate(),
                enrollment.getStatus(),
                enrollment.getNotes(),
                enrollment.getOrganization().getId(),
                enrollment.getBatch().getId(),
                enrollment.getBatch().getName(),
                enrollment.getBatch().getCode(),
                enrollment.getStudent().getId(),
                studentName.trim(),
                enrollment.getStudent().getEmail()
        );
    }


    // =========================================================
    // ATTENDANCE RESPONSE
    // =========================================================

    private AttendanceResponse toAttendanceResponse(
            Attendance attendance
    ) {

        String studentName =
                attendance.getStudent()
                        .getFirstName()
                        + " "
                        + (
                        attendance.getStudent()
                                .getLastName() != null
                                ? attendance.getStudent()
                                        .getLastName()
                                : ""
                );

        return new AttendanceResponse(
                attendance.getId(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getRemarks(),
                attendance.getOrganization().getId(),
                attendance.getBatch().getId(),
                attendance.getBatch().getName(),
                attendance.getBatch().getCode(),
                attendance.getStudent().getId(),
                studentName.trim(),
                attendance.getStudent().getEmail()
        );
    }


    // =========================================================
    // FEE RESPONSE
    // =========================================================

    private FeeResponse toFeeResponse(
            FeeAccount feeAccount
    ) {

        Enrollment enrollment =
                feeAccount.getEnrollment();

        User student =
                enrollment.getStudent();

        String studentName =
                student.getFirstName()
                        + " "
                        + (
                        student.getLastName() != null
                                ? student.getLastName()
                                : ""
                );

        return new FeeResponse(
                feeAccount.getId(),
                enrollment.getId(),
                feeAccount.getOrganization().getId(),
                student.getId(),
                studentName.trim(),
                student.getEmail(),
                enrollment.getBatch().getId(),
                enrollment.getBatch().getName(),
                enrollment.getBatch().getCode(),
                enrollment.getBatch()
                        .getCourse()
                        .getName(),
                feeAccount.getTotalAmount(),
                feeAccount.getDiscountAmount(),
                feeAccount.getPayableAmount(),
                feeAccount.getAmountPaid(),
                feeAccount.getBalanceAmount(),
                feeAccount.getDueDate(),
                feeAccount.getStatus(),
                feeAccount.isActive()
        );
    }
}