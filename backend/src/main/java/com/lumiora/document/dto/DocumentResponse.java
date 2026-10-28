package com.lumiora.document.dto;

import com.lumiora.document.entity.DocumentCategory;

public record DocumentResponse(
        Long id,
        String title,
        String description,
        DocumentCategory category,
        String originalFilename,
        String contentType,
        long sizeBytes,
        boolean active,
        Long organizationId,
        Long ownerUserId,
        String ownerName,
        Long uploadedById,
        String uploadedByName
) {
}