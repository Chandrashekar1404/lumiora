package com.lumiora.material.dto;

import com.lumiora.material.entity.MaterialType;

public record MaterialResponse(
        Long id,
        String title,
        String description,
        MaterialType type,
        String resourceUrl,
        boolean active,
        Long organizationId,
        String organizationName,
        Long courseId,
        String courseName,
        Long batchId,
        String batchName,
        Long createdById,
        String createdByName
) {
}