package com.lumiora.document.dto;

public record DownloadUrlResponse(
        Long documentId,
        String downloadPath
) {
}