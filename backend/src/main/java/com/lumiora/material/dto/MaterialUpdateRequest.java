package com.lumiora.material.dto;

import com.lumiora.material.entity.MaterialType;

import jakarta.validation.constraints.Size;

public record MaterialUpdateRequest(

        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        MaterialType type,

        @Size(max = 2048)
        String resourceUrl

) {
}