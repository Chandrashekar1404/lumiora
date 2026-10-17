package com.lumiora.material.controller;

import com.lumiora.dto.response.ApiResponse;
import com.lumiora.material.dto.MaterialCreateRequest;
import com.lumiora.material.dto.MaterialUpdateRequest;
import com.lumiora.material.service.MaterialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(
            @Valid @RequestBody MaterialCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Material created successfully",
                        materialService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Materials retrieved successfully",
                        materialService.getAll()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMine() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Your accessible materials retrieved successfully",
                        materialService.getMine()));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<ApiResponse<?>> getByBatch(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch materials retrieved successfully",
                        materialService.getByBatch(batchId)));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<?>> getByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Course materials retrieved successfully",
                        materialService.getByCourse(courseId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Material retrieved successfully",
                        materialService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Material updated successfully",
                        materialService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deactivate(
            @PathVariable Long id) {

        materialService.deactivate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Material deactivated successfully",
                        null));
    }
}