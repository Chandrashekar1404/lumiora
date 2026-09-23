package com.lumiora.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardResponse {

    private String scopeType;

    private Long organizationId;

    private long totalOrganizations;

    private long totalStudents;
    private long totalTrainers;
    private long totalCounselors;
    private long totalAccountants;

    private long totalCourses;
    private long activeCourses;

    private long totalBatches;
    private long upcomingBatches;
    private long ongoingBatches;
    private long completedBatches;
    private long cancelledBatches;

    private long totalEnrollments;
    private long enrolledEnrollments;
    private long completedEnrollments;
    private long droppedEnrollments;
    private long cancelledEnrollments;

    private long totalAttendanceRecords;
    private long presentAttendance;
    private long absentAttendance;
    private long lateAttendance;
    private long halfDayAttendance;

    private long totalLeads;
    private long newLeads;
    private long contactedLeads;
    private long interestedLeads;
    private long followUpLeads;
    private long convertedLeads;
    private long lostLeads;
}