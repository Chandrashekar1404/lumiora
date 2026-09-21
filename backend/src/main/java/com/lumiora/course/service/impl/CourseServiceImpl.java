package com.lumiora.course.service.impl;

import com.lumiora.course.entity.Course;
import com.lumiora.course.repository.CourseRepository;
import com.lumiora.course.service.CourseService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> findAllByOrganizationId(Long organizationId) {
        return courseRepository.findAllByOrganization_Id(
                organizationId
        );
    }

    @Override
    public Optional<Course> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return courseRepository.findByIdAndOrganization_Id(
                id,
                organizationId
        );
    }

    @Override
    public boolean existsByCodeAndOrganizationId(
            String code,
            Long organizationId) {

        return courseRepository.existsByCodeAndOrganization_Id(
                code,
                organizationId
        );
    }
}