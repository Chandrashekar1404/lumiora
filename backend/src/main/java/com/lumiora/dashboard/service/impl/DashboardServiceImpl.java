package com.lumiora.dashboard.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumiora.attendance.entity.Attendance;
import com.lumiora.attendance.entity.AttendanceStatus;
import com.lumiora.attendance.repository.AttendanceRepository;

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.entity.BatchStatus;
import com.lumiora.batch.repository.BatchRepository;

import com.lumiora.course.entity.Course;
import com.lumiora.course.repository.CourseRepository;

import com.lumiora.dashboard.dto.DashboardResponse;
import com.lumiora.dashboard.dto.FinanceDashboardResponse;
import com.lumiora.dashboard.service.DashboardService;

import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.repository.EnrollmentRepository;

import com.lumiora.entity.auth.User;

import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.FeeStatus;
import com.lumiora.fee.entity.Payment;
import com.lumiora.fee.entity.PaymentStatus;
import com.lumiora.fee.repository.FeeAccountRepository;
import com.lumiora.fee.repository.PaymentRepository;

import com.lumiora.lead.entity.Lead;
import com.lumiora.lead.entity.LeadStatus;
import com.lumiora.lead.repository.LeadRepository;

import com.lumiora.organization.repository.OrganizationRepository;

import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final CourseRepository courseRepository;

    private final BatchRepository batchRepository;

    private final EnrollmentRepository enrollmentRepository;

    private final AttendanceRepository attendanceRepository;

    private final LeadRepository leadRepository;

    private final FeeAccountRepository feeAccountRepository;

    private final PaymentRepository paymentRepository;


    @Override
    public DashboardResponse getDashboard(
            Authentication authentication
    ) {

        User currentUser = getCurrentUser(authentication);

        String role = currentUser.getRole().getName();

        List<User> students;
        List<User> trainers;
        List<User> counselors;
        List<User> accountants;

        List<Course> courses;

        List<Batch> batches;

        List<Enrollment> enrollments;

        List<Attendance> attendanceRecords;

        List<Lead> leads;

        DashboardResponse response =
                new DashboardResponse();


        /*
         * SUPER ADMIN
         * ----------
         * Can see data across all organizations.
         */
        if ("SUPER_ADMIN".equals(role)) {

            response.setScopeType("GLOBAL");

            response.setOrganizationId(null);

            response.setTotalOrganizations(
                    organizationRepository.count()
            );

            students =
                    userRepository.findAllByRole_Name(
                            "STUDENT"
                    );

            trainers =
                    userRepository.findAllByRole_Name(
                            "TRAINER"
                    );

            counselors =
                    userRepository.findAllByRole_Name(
                            "COUNSELOR"
                    );

            accountants =
                    userRepository.findAllByRole_Name(
                            "ACCOUNTANT"
                    );

            courses =
                    courseRepository.findAll();

            batches =
                    batchRepository.findAll();

            enrollments =
                    enrollmentRepository.findAll();

            attendanceRecords =
                    attendanceRepository.findAll();

            leads =
                    leadRepository.findAll();

        }

        /*
         * ADMIN
         * -----
         * Can see only their own organization.
         */
        else if ("ADMIN".equals(role)) {

            Long organizationId =
                    getOrganizationId(currentUser);

            response.setScopeType("ORGANIZATION");

            response.setOrganizationId(
                    organizationId
            );

            response.setTotalOrganizations(1);

            students =
                    userRepository
                            .findAllByOrganization_IdAndRole_Name(
                                    organizationId,
                                    "STUDENT"
                            );

            trainers =
                    userRepository
                            .findAllByOrganization_IdAndRole_Name(
                                    organizationId,
                                    "TRAINER"
                            );

            counselors =
                    userRepository
                            .findAllByOrganization_IdAndRole_Name(
                                    organizationId,
                                    "COUNSELOR"
                            );

            accountants =
                    userRepository
                            .findAllByOrganization_IdAndRole_Name(
                                    organizationId,
                                    "ACCOUNTANT"
                            );

            courses =
                    courseRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

            batches =
                    batchRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

            enrollments =
                    enrollmentRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

            attendanceRecords =
                    attendanceRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

            leads =
                    leadRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

        }

        else {

            throw new IllegalArgumentException(
                    "Dashboard access is available only for SUPER_ADMIN and ADMIN"
            );
        }


        /*
         * USERS
         */

        response.setTotalStudents(
                students.size()
        );

        response.setTotalTrainers(
                trainers.size()
        );

        response.setTotalCounselors(
                counselors.size()
        );

        response.setTotalAccountants(
                accountants.size()
        );


        /*
         * COURSES
         */

        response.setTotalCourses(
                courses.size()
        );

        response.setActiveCourses(
                courses.stream()
                        .filter(Course::isActive)
                        .count()
        );


        /*
         * BATCHES
         */

        response.setTotalBatches(
                batches.size()
        );

        response.setUpcomingBatches(
                countBatchStatus(
                        batches,
                        BatchStatus.UPCOMING
                )
        );

        response.setOngoingBatches(
                countBatchStatus(
                        batches,
                        BatchStatus.ONGOING
                )
        );

        response.setCompletedBatches(
                countBatchStatus(
                        batches,
                        BatchStatus.COMPLETED
                )
        );

        response.setCancelledBatches(
                countBatchStatus(
                        batches,
                        BatchStatus.CANCELLED
                )
        );


        /*
         * ENROLLMENTS
         */

        response.setTotalEnrollments(
                enrollments.size()
        );

        response.setEnrolledEnrollments(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.ENROLLED
                )
        );

        response.setCompletedEnrollments(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.COMPLETED
                )
        );

        response.setDroppedEnrollments(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.DROPPED
                )
        );

        response.setCancelledEnrollments(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.CANCELLED
                )
        );


        /*
         * ATTENDANCE
         */

        response.setTotalAttendanceRecords(
                attendanceRecords.size()
        );

        response.setPresentAttendance(
                countAttendanceStatus(
                        attendanceRecords,
                        AttendanceStatus.PRESENT
                )
        );

        response.setAbsentAttendance(
                countAttendanceStatus(
                        attendanceRecords,
                        AttendanceStatus.ABSENT
                )
        );

        response.setLateAttendance(
                countAttendanceStatus(
                        attendanceRecords,
                        AttendanceStatus.LATE
                )
        );

        response.setHalfDayAttendance(
                countAttendanceStatus(
                        attendanceRecords,
                        AttendanceStatus.HALF_DAY
                )
        );


        /*
         * LEADS
         */

        response.setTotalLeads(
                leads.size()
        );

        response.setNewLeads(
                countLeadStatus(
                        leads,
                        LeadStatus.NEW
                )
        );

        response.setContactedLeads(
                countLeadStatus(
                        leads,
                        LeadStatus.CONTACTED
                )
        );

        response.setInterestedLeads(
                countLeadStatus(
                        leads,
                        LeadStatus.INTERESTED
                )
        );

        response.setFollowUpLeads(
                countLeadStatus(
                        leads,
                        LeadStatus.FOLLOW_UP
                )
        );

        response.setConvertedLeads(
                countLeadStatus(
                        leads,
                        LeadStatus.CONVERTED
                )
        );

        response.setLostLeads(
                countLeadStatus(
                        leads,
                        LeadStatus.LOST
                )
        );


        return response;
    }


    @Override
    public FinanceDashboardResponse getFinanceDashboard(
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        String role =
                currentUser.getRole().getName();

        List<FeeAccount> feeAccounts;

        List<Payment> payments;

        FinanceDashboardResponse response =
                new FinanceDashboardResponse();


        /*
         * SUPER ADMIN
         */

        if ("SUPER_ADMIN".equals(role)) {

            response.setScopeType("GLOBAL");

            response.setOrganizationId(null);

            feeAccounts =
                    feeAccountRepository.findAll();

            payments =
                    paymentRepository.findAll();

        }

        /*
         * ADMIN / ACCOUNTANT
         */

        else if (
                "ADMIN".equals(role)
                        ||
                "ACCOUNTANT".equals(role)
        ) {

            Long organizationId =
                    getOrganizationId(currentUser);

            response.setScopeType(
                    "ORGANIZATION"
            );

            response.setOrganizationId(
                    organizationId
            );

            feeAccounts =
                    feeAccountRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

            payments =
                    paymentRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            );

        }

        else {

            throw new IllegalArgumentException(
                    "Finance dashboard access is available only for SUPER_ADMIN, ADMIN and ACCOUNTANT"
            );
        }


        /*
         * FEE ACCOUNT COUNTS
         */

        response.setTotalFeeAccounts(
                feeAccounts.size()
        );

        response.setPendingFeeAccounts(
                countFeeStatus(
                        feeAccounts,
                        FeeStatus.PENDING
                )
        );

        response.setPartiallyPaidFeeAccounts(
                countFeeStatus(
                        feeAccounts,
                        FeeStatus.PARTIALLY_PAID
                )
        );

        response.setPaidFeeAccounts(
                countFeeStatus(
                        feeAccounts,
                        FeeStatus.PAID
                )
        );

        response.setCancelledFeeAccounts(
                countFeeStatus(
                        feeAccounts,
                        FeeStatus.CANCELLED
                )
        );


        /*
         * FEE TOTALS
         */

        response.setTotalFees(
                sumFeeAmount(
                        feeAccounts,
                        FeeAccount::getTotalAmount
                )
        );

        response.setTotalDiscount(
                sumFeeAmount(
                        feeAccounts,
                        FeeAccount::getDiscountAmount
                )
        );

        response.setTotalPayable(
                sumFeeAmount(
                        feeAccounts,
                        FeeAccount::getPayableAmount
                )
        );

        response.setTotalPaid(
                sumFeeAmount(
                        feeAccounts,
                        FeeAccount::getAmountPaid
                )
        );

        response.setTotalBalance(
                sumFeeAmount(
                        feeAccounts,
                        FeeAccount::getBalanceAmount
                )
        );


        /*
         * OVERDUE
         */

        LocalDate today =
                LocalDate.now();

        long overdueCount =
                feeAccounts.stream()
                        .filter(FeeAccount::isActive)
                        .filter(fee ->
                                fee.getBalanceAmount() != null
                                        &&
                                fee.getBalanceAmount()
                                        .compareTo(
                                                BigDecimal.ZERO
                                        ) > 0
                        )
                        .filter(fee ->
                                fee.getDueDate() != null
                                        &&
                                fee.getDueDate()
                                        .isBefore(today)
                        )
                        .count();

        response.setOverdueFeeAccounts(
                overdueCount
        );


        /*
         * PAYMENT COUNTS
         */

        response.setTotalPayments(
                payments.size()
        );

        response.setSuccessfulPaymentCount(
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.SUCCESS
                        )
                        .count()
        );

        response.setCancelledPaymentCount(
                payments.stream()
                        .filter(payment ->
                                payment.getStatus()
                                        == PaymentStatus.CANCELLED
                        )
                        .count()
        );


        /*
         * PAYMENT AMOUNTS
         */

        response.setSuccessfulPaymentAmount(
                sumPaymentAmount(
                        payments,
                        PaymentStatus.SUCCESS
                )
        );

        response.setCancelledPaymentAmount(
                sumPaymentAmount(
                        payments,
                        PaymentStatus.CANCELLED
                )
        );


        return response;
    }


    private User getCurrentUser(
            Authentication authentication
    ) {

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }


    private Long getOrganizationId(
            User user
    ) {

        if (user.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "User is not associated with an organization"
            );
        }

        return user
                .getOrganization()
                .getId();
    }


    private long countBatchStatus(
            List<Batch> batches,
            BatchStatus status
    ) {

        return batches.stream()
                .filter(batch ->
                        batch.getStatus() == status
                )
                .count();
    }


    private long countEnrollmentStatus(
            List<Enrollment> enrollments,
            EnrollmentStatus status
    ) {

        return enrollments.stream()
                .filter(enrollment ->
                        enrollment.getStatus() == status
                )
                .count();
    }


    private long countAttendanceStatus(
            List<Attendance> attendanceRecords,
            AttendanceStatus status
    ) {

        return attendanceRecords.stream()
                .filter(attendance ->
                        attendance.getStatus() == status
                )
                .count();
    }


    private long countLeadStatus(
            List<Lead> leads,
            LeadStatus status
    ) {

        return leads.stream()
                .filter(lead ->
                        lead.getStatus() == status
                )
                .count();
    }


    private long countFeeStatus(
            List<FeeAccount> feeAccounts,
            FeeStatus status
    ) {

        return feeAccounts.stream()
                .filter(fee ->
                        fee.getStatus() == status
                )
                .count();
    }


    private BigDecimal sumFeeAmount(
            List<FeeAccount> feeAccounts,
            Function<FeeAccount, BigDecimal> extractor
    ) {

        return feeAccounts.stream()
                .map(extractor)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    private BigDecimal sumPaymentAmount(
            List<Payment> payments,
            PaymentStatus status
    ) {

        return payments.stream()
                .filter(payment ->
                        payment.getStatus() == status
                )
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}