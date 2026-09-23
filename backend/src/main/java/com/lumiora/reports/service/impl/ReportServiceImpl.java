package com.lumiora.reports.service.impl;

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

import com.lumiora.course.repository.CourseRepository;

import com.lumiora.batch.repository.BatchRepository;

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

import com.lumiora.reports.dto.AttendanceReportResponse;
import com.lumiora.reports.dto.EnrollmentReportResponse;
import com.lumiora.reports.dto.FinanceReportResponse;
import com.lumiora.reports.dto.LeadReportResponse;
import com.lumiora.reports.dto.OverviewReportResponse;
import com.lumiora.reports.service.ReportService;

import com.lumiora.organization.repository.OrganizationRepository;
import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

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
    public OverviewReportResponse getOverviewReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        validateDates(fromDate, toDate);

        User user = getCurrentUser(authentication);

        String role = user.getRole().getName();

        OverviewReportResponse response =
                new OverviewReportResponse();

        response.setFromDate(
                fromDate != null ? fromDate.toString() : null
        );

        response.setToDate(
                toDate != null ? toDate.toString() : null
        );


        List<Enrollment> enrollments =
                getEnrollments(user, role);

        List<Attendance> attendance =
                getAttendance(user, role);

        List<Lead> leads =
                getLeads(user, role);


        response.setTotalStudents(
                getUsers(user, role, "STUDENT").size()
        );

        response.setTotalTrainers(
                getUsers(user, role, "TRAINER").size()
        );

        response.setTotalCounselors(
                getUsers(user, role, "COUNSELOR").size()
        );

        response.setTotalAccountants(
                getUsers(user, role, "ACCOUNTANT").size()
        );


        if ("SUPER_ADMIN".equals(role)) {

            response.setScopeType("GLOBAL");
            response.setOrganizationId(null);

            response.setTotalCourses(
                    courseRepository.count()
            );

            response.setTotalBatches(
                    batchRepository.count()
            );

        } else if ("ADMIN".equals(role)) {

            Long organizationId =
                    getOrganizationId(user);

            response.setScopeType("ORGANIZATION");
            response.setOrganizationId(
                    organizationId
            );

            response.setTotalCourses(
                    courseRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            )
                            .size()
            );

            response.setTotalBatches(
                    batchRepository
                            .findAllByOrganization_Id(
                                    organizationId
                            )
                            .size()
            );

        } else {

            throw new IllegalArgumentException(
                    "Overview reports are available only for SUPER_ADMIN and ADMIN"
            );
        }


        response.setTotalEnrollments(
                filterByDate(
                        enrollments,
                        Enrollment::getEnrollmentDate,
                        fromDate,
                        toDate
                ).size()
        );

        response.setTotalAttendanceRecords(
                filterByDate(
                        attendance,
                        Attendance::getAttendanceDate,
                        fromDate,
                        toDate
                ).size()
        );

        List<Lead> filteredLeads =
                filterByDate(
                        leads,
                        Lead::getFollowUpDate,
                        fromDate,
                        toDate
                );

        response.setTotalLeads(
                filteredLeads.size()
        );

        response.setConvertedLeads(
                countLeadStatus(
                        filteredLeads,
                        LeadStatus.CONVERTED
                )
        );

        return response;
    }


    @Override
    public FinanceReportResponse getFinanceReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        validateDates(fromDate, toDate);

        User user = getCurrentUser(authentication);

        String role = user.getRole().getName();

        FinanceReportResponse response =
                new FinanceReportResponse();

        List<FeeAccount> feeAccounts;

        List<Payment> payments;


        if ("SUPER_ADMIN".equals(role)) {

            response.setScopeType("GLOBAL");
            response.setOrganizationId(null);

            feeAccounts =
                    feeAccountRepository.findAll();

            payments =
                    paymentRepository.findAll();

        } else if ("ADMIN".equals(role)
                || "ACCOUNTANT".equals(role)) {

            Long organizationId =
                    getOrganizationId(user);

            response.setScopeType("ORGANIZATION");
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

        } else {

            throw new IllegalArgumentException(
                    "Finance reports are available only for SUPER_ADMIN, ADMIN and ACCOUNTANT"
            );
        }


        response.setFromDate(
                fromDate != null ? fromDate.toString() : null
        );

        response.setToDate(
                toDate != null ? toDate.toString() : null
        );


        /*
         * Fee accounts are associated with enrollment dates.
         */
        feeAccounts =
                feeAccounts.stream()
                        .filter(fee -> {

                            if (fromDate == null
                                    && toDate == null) {
                                return true;
                            }

                            if (fee.getEnrollment() == null
                                    || fee.getEnrollment()
                                    .getEnrollmentDate() == null) {
                                return false;
                            }

                            return isWithinRange(
                                    fee.getEnrollment()
                                            .getEnrollmentDate(),
                                    fromDate,
                                    toDate
                            );

                        })
                        .toList();


        /*
         * Payments are filtered by payment date.
         */
        payments =
                payments.stream()
                        .filter(payment ->
                                isWithinRange(
                                        payment.getPaymentDate(),
                                        fromDate,
                                        toDate
                                )
                        )
                        .toList();


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


        response.setTotalPayments(
                payments.size()
        );

        response.setSuccessfulPayments(
                countPaymentStatus(
                        payments,
                        PaymentStatus.SUCCESS
                )
        );

        response.setCancelledPayments(
                countPaymentStatus(
                        payments,
                        PaymentStatus.CANCELLED
                )
        );

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


    @Override
    public AttendanceReportResponse getAttendanceReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        validateDates(fromDate, toDate);

        User user = getCurrentUser(authentication);

        String role = user.getRole().getName();

        AttendanceReportResponse response =
                new AttendanceReportResponse();

        List<Attendance> attendance =
                getAttendance(user, role);

        attendance =
                filterByDate(
                        attendance,
                        Attendance::getAttendanceDate,
                        fromDate,
                        toDate
                );


        setScope(
                response,
                user,
                role,
                fromDate,
                toDate
        );


        response.setTotalRecords(
                attendance.size()
        );

        response.setPresentCount(
                countAttendanceStatus(
                        attendance,
                        AttendanceStatus.PRESENT
                )
        );

        response.setAbsentCount(
                countAttendanceStatus(
                        attendance,
                        AttendanceStatus.ABSENT
                )
        );

        response.setLateCount(
                countAttendanceStatus(
                        attendance,
                        AttendanceStatus.LATE
                )
        );

        response.setHalfDayCount(
                countAttendanceStatus(
                        attendance,
                        AttendanceStatus.HALF_DAY
                )
        );


        long attendanceCount =
                response.getPresentCount()
                        + response.getAbsentCount()
                        + response.getLateCount()
                        + response.getHalfDayCount();

        if (attendanceCount == 0) {

            response.setAttendancePercentage(
                    BigDecimal.ZERO
            );

        } else {

            BigDecimal percentage =
                    BigDecimal.valueOf(
                            response.getPresentCount()
                                    * 100.0
                                    / attendanceCount
                    );

            response.setAttendancePercentage(
                    percentage.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    )
            );
        }


        return response;
    }


    @Override
    public EnrollmentReportResponse getEnrollmentReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        validateDates(fromDate, toDate);

        User user = getCurrentUser(authentication);

        String role = user.getRole().getName();

        List<Enrollment> enrollments =
                getEnrollments(user, role);

        enrollments =
                filterByDate(
                        enrollments,
                        Enrollment::getEnrollmentDate,
                        fromDate,
                        toDate
                );


        EnrollmentReportResponse response =
                new EnrollmentReportResponse();

        setScope(
                response,
                user,
                role,
                fromDate,
                toDate
        );

        response.setTotalEnrollments(
                enrollments.size()
        );

        response.setEnrolledCount(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.ENROLLED
                )
        );

        response.setCompletedCount(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.COMPLETED
                )
        );

        response.setDroppedCount(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.DROPPED
                )
        );

        response.setCancelledCount(
                countEnrollmentStatus(
                        enrollments,
                        EnrollmentStatus.CANCELLED
                )
        );

        return response;
    }


    @Override
    public LeadReportResponse getLeadReport(
            Authentication authentication,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        validateDates(fromDate, toDate);

        User user = getCurrentUser(authentication);

        String role = user.getRole().getName();

        List<Lead> leads =
                getLeads(user, role);

        leads =
                filterByDate(
                        leads,
                        Lead::getFollowUpDate,
                        fromDate,
                        toDate
                );


        LeadReportResponse response =
                new LeadReportResponse();

        setScope(
                response,
                user,
                role,
                fromDate,
                toDate
        );


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


        long total =
                response.getTotalLeads();

        if (total == 0) {

            response.setConversionPercentage(
                    BigDecimal.ZERO
            );

        } else {

            BigDecimal percentage =
                    BigDecimal.valueOf(
                            response.getConvertedLeads()
                                    * 100.0
                                    / total
                    );

            response.setConversionPercentage(
                    percentage.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    )
            );
        }


        return response;
    }


    private User getCurrentUser(
            Authentication authentication
    ) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }


    private Long getOrganizationId(User user) {

        if (user.getOrganization() == null) {

            throw new IllegalArgumentException(
                    "User is not associated with an organization"
            );
        }

        return user.getOrganization().getId();
    }


    private List<User> getUsers(
            User currentUser,
            String role,
            String targetRole
    ) {

        if ("SUPER_ADMIN".equals(role)) {

            return userRepository
                    .findAllByRole_Name(
                            targetRole
                    );
        }

        if ("ADMIN".equals(role)) {

            return userRepository
                    .findAllByOrganization_IdAndRole_Name(
                            getOrganizationId(currentUser),
                            targetRole
                    );
        }

        throw new IllegalArgumentException(
                "Report access denied"
        );
    }


    private List<Enrollment> getEnrollments(
            User user,
            String role
    ) {

        if ("SUPER_ADMIN".equals(role)) {

            return enrollmentRepository.findAll();
        }

        if ("ADMIN".equals(role)) {

            return enrollmentRepository
                    .findAllByOrganization_Id(
                            getOrganizationId(user)
                    );
        }

        throw new IllegalArgumentException(
                "Enrollment report access denied"
        );
    }


    private List<Attendance> getAttendance(
            User user,
            String role
    ) {

        if ("SUPER_ADMIN".equals(role)) {

            return attendanceRepository.findAll();
        }

        if ("ADMIN".equals(role)) {

            return attendanceRepository
                    .findAllByOrganization_Id(
                            getOrganizationId(user)
                    );
        }

        throw new IllegalArgumentException(
                "Attendance report access denied"
        );
    }


    private List<Lead> getLeads(
            User user,
            String role
    ) {

        if ("SUPER_ADMIN".equals(role)) {

            return leadRepository.findAll();
        }

        if ("ADMIN".equals(role)) {

            return leadRepository
                    .findAllByOrganization_Id(
                            getOrganizationId(user)
                    );
        }

        throw new IllegalArgumentException(
                "Lead report access denied"
        );
    }


    private <T> List<T> filterByDate(
            List<T> records,
            Function<T, LocalDate> dateExtractor,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return records.stream()
                .filter(record -> {

                    LocalDate date =
                            dateExtractor.apply(record);

                    return date != null
                            && isWithinRange(
                                    date,
                                    fromDate,
                                    toDate
                            );
                })
                .toList();
    }


    private boolean isWithinRange(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (date == null) {
            return false;
        }

        if (fromDate != null
                && date.isBefore(fromDate)) {

            return false;
        }

        if (toDate != null
                && date.isAfter(toDate)) {

            return false;
        }

        return true;
    }


    private void validateDates(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new IllegalArgumentException(
                    "fromDate cannot be after toDate"
            );
        }
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
            List<Attendance> records,
            AttendanceStatus status
    ) {

        return records.stream()
                .filter(attendance ->
                        attendance.getStatus() == status
                )
                .count();
    }


    private long countFeeStatus(
            List<FeeAccount> fees,
            FeeStatus status
    ) {

        return fees.stream()
                .filter(fee ->
                        fee.getStatus() == status
                )
                .count();
    }


    private long countPaymentStatus(
            List<Payment> payments,
            PaymentStatus status
    ) {

        return payments.stream()
                .filter(payment ->
                        payment.getStatus() == status
                )
                .count();
    }


    private BigDecimal sumFeeAmount(
            List<FeeAccount> fees,
            Function<FeeAccount, BigDecimal> extractor
    ) {

        return fees.stream()
                .map(extractor)
                .filter(value -> value != null)
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
                .filter(value -> value != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    private void setScope(
            Object response,
            User user,
            String role,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        Long organizationId = null;

        String scopeType;


        if ("SUPER_ADMIN".equals(role)) {

            scopeType = "GLOBAL";

        } else if ("ADMIN".equals(role)) {

            scopeType = "ORGANIZATION";

            organizationId =
                    getOrganizationId(user);

        } else {

            throw new IllegalArgumentException(
                    "Report access denied"
            );
        }


        if (response instanceof AttendanceReportResponse r) {

            r.setScopeType(scopeType);
            r.setOrganizationId(organizationId);

            r.setFromDate(
                    fromDate != null
                            ? fromDate.toString()
                            : null
            );

            r.setToDate(
                    toDate != null
                            ? toDate.toString()
                            : null
            );

        } else if (response instanceof EnrollmentReportResponse r) {

            r.setScopeType(scopeType);
            r.setOrganizationId(organizationId);

            r.setFromDate(
                    fromDate != null
                            ? fromDate.toString()
                            : null
            );

            r.setToDate(
                    toDate != null
                            ? toDate.toString()
                            : null
            );

        } else if (response instanceof LeadReportResponse r) {

            r.setScopeType(scopeType);
            r.setOrganizationId(organizationId);

            r.setFromDate(
                    fromDate != null
                            ? fromDate.toString()
                            : null
            );

            r.setToDate(
                    toDate != null
                            ? toDate.toString()
                            : null
            );
        }
    }
}