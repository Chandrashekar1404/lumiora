package com.lumiora.course.service;

import com.lumiora.course.entity.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    Course save(Course course);

    List<Course> findAll();

    Optional<Course> findById(Long id);

    List<Course> findAllByOrganizationId(Long organizationId);

    Optional<Course> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByCodeAndOrganizationId(
            String code,
            Long organizationId
    );
}