
package com.lumiora.audit.service;

import com.lumiora.audit.entity.AuditAction;
import com.lumiora.audit.entity.AuditLog;
import com.lumiora.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog record(
            Long actorUserId,
            String actorEmail,
            AuditAction action,
            String entityType,
            Long entityId,
            Long organizationId,
            String details) {

        AuditLog log = new AuditLog(
                actorUserId,
                actorEmail,
                action,
                entityType,
                entityId,
                organizationId,
                details
        );

        return auditLogRepository.save(log);
    }
}
