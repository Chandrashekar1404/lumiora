package com.lumiora.course.dto;

import com.lumiora.course.entity.Course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;

    private String name;

    private String code;

    private String description;

    private String duration;

    private Double fee;

    private boolean active;

    private Long organizationId;
}