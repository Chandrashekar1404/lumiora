package com.lumiora.document.controller;

import com.lumiora.document.dto.DocumentResponse;
import com.lumiora.document.entity.DocumentCategory;
import com.lumiora.document.service.DocumentService;
import com.lumiora.dto.response.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> upload(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "OTHER") DocumentCategory category,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Long ownerUserId,
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response = documentService.upload(
                title, description, category,
                organizationId, ownerUserId, file);

        return ResponseEntity.status(201)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping
    public ApiResponse<List<DocumentResponse>> getAll() {
        return ApiResponse.success(
                "Documents retrieved successfully",
                documentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(
                "Document retrieved successfully",
                documentService.getById(id));
    }

    @GetMapping("/student/{studentUserId}")
    public ApiResponse<List<DocumentResponse>> getByStudent(
            @PathVariable Long studentUserId) {
        return ApiResponse.success(
                "Student documents retrieved successfully",
                documentService.getByStudent(studentUserId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentResponse document = documentService.getById(id);
        Resource resource = documentService.download(id);

        MediaType mediaType = switch (document.contentType()) {
            case "application/pdf" -> MediaType.APPLICATION_PDF;
            case "image/jpeg" -> MediaType.IMAGE_JPEG;
            case "image/png" -> MediaType.IMAGE_PNG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(document.originalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }

    @PutMapping("/{id}")
    public ApiResponse<DocumentResponse> updateMetadata(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) DocumentCategory category) {

        return ApiResponse.success(
                "Document metadata updated successfully",
                documentService.updateMetadata(id, title, description, category));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deactivate(@PathVariable Long id) {
        documentService.deactivate(id);

        return ApiResponse.success(
                "Document deactivated successfully",
                "The file is retained in local storage");
    }
}