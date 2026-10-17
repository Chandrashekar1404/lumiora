package com.lumiora.material.dto;

import com.lumiora.material.entity.MaterialType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaterialCreateRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull
        MaterialType type,

        @NotBlank
        @Size(max = 2048)
        String resourceUrl,

        @NotNull
        Long organizationId,

        Long courseId,

        Long batchId

) {
}