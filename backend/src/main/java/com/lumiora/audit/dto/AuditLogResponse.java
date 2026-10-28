package com.lumiora.audit.dto;

import com.lumiora.audit.entity.AuditAction;
import com.lumiora.audit.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorEmail,
        AuditAction action,
        String entityType,
        Long entityId,
        Long organizationId,
        String details,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorUserId(),
                log.getActorEmail(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOrganizationId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
