package com.lumiora.course.repository;

import com.lumiora.course.entity.Course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOrganization_Id(Long organizationId);

    Optional<Course> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    boolean existsByCodeAndOrganization_Id(
            String code,
            Long organizationId
    );
}