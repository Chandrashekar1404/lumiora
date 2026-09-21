package com.lumiora.course.dto;

import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateRequest {

    private String name;

    private String description;

    private String duration;

    @PositiveOrZero(message = "Course fee cannot be negative")
    private Double fee;

    private Boolean active;
}