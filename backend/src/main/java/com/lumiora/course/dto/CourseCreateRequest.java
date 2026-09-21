package com.lumiora.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateRequest {

    @NotBlank(message = "Course name is required")
    private String name;

    @NotBlank(message = "Course code is required")
    private String code;

    private String description;

    private String duration;

    @NotNull(message = "Course fee is required")
    @PositiveOrZero(message = "Course fee cannot be negative")
    private Double fee;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;
}