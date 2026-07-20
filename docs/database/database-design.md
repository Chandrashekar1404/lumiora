# Database Design

## Overview

The Lumiora database is designed using a relational database model with scalability, maintainability, and future SaaS expansion in mind.

The system follows normalization principles while maintaining performance for enterprise-level operations.

---

# Design Principles

- Single source of truth
- Normalized relationships
- Role-based access control
- Multi-organization ready
- AI-ready architecture
- Audit-friendly
- Extensible

---

# Core Business Entities

## Organization

Represents an educational organization using Lumiora.

Examples:

- College
- Training Institute
- School
- Coaching Center

---

## User

Represents every authenticated user in the platform.

Examples:

- Admin
- Trainer
- Student
- Counselor
- Accountant

Authentication is handled only through this entity.

---

## Role

Defines system roles.

Examples:

- Super Admin
- Admin
- Trainer
- Student
- Counselor
- Accountant

---

## Permission

Stores application permissions.

Examples:

- STUDENT_CREATE
- STUDENT_UPDATE
- STUDENT_DELETE
- COURSE_MANAGE

---

## Student

Stores student-specific information.

Linked with User.

---

## Trainer

Stores trainer-specific information.

Linked with User.

---

## Course

Represents available courses.

Examples:

- Java Full Stack
- Python Full Stack
- MERN Stack

---

## Batch

Represents a running batch for a course.

A course can have multiple batches.

---

## Enrollment

Represents student enrollment into a batch.

Acts as the bridge between Student and Batch.

---

## Attendance

Stores daily attendance records.

---

## Assignment

Stores assignments created by trainers.

---

## Submission

Stores assignment submissions.

---

## Examination

Stores examination details.

---

## Result

Stores examination results.

---

## Fee

Stores fee details.

---

## Payment

Stores payment transactions.

---

## Notification

Stores in-app notifications.

---

## AI Insight

Stores AI-generated summaries and recommendations.

Examples:

- Student Risk Analysis
- Fee Collection Forecast
- Attendance Prediction
- Performance Summary

---

## Audit Log

Stores important system activities.

Examples:

- User Login
- Student Created
- Fee Updated
- Course Deleted

---

# Database Goals

- Secure
- Scalable
- Maintainable
- Cloud Ready
- AI Ready
- Multi Organization Support
