
package com.lumiora.audit.controller;

import com.lumiora.audit.dto.AuditLogResponse;
import com.lumiora.audit.entity.AuditAction;
import com.lumiora.audit.entity.AuditLog;
import com.lumiora.audit.repository.AuditLogRepository;
import com.lumiora.entity.auth.User;
import com.lumiora.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogController(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public Page<AuditLogResponse> getAuditLogs(
            Authentication authentication,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' must be before or equal to 'to'"
            );
        }

        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        boolean isSuperAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_SUPER_ADMIN"));

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<AuditLog> spec = (root, query, cb) ->
                cb.conjunction();

        // Super Admin can view all organizations.
        // Admin can view only their own organization's logs.
        if (!isSuperAdmin) {
            if (currentUser.getOrganization() == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "User has no organization assigned"
                );
            }

            Long organizationId = currentUser.getOrganization().getId();

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("organizationId"), organizationId));
        }

        if (actorUserId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("actorUserId"), actorUserId));
        }

        if (action != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("action"), action));
        }

        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("entityType"), entityType));
        }

        if (entityId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("entityId"), entityId));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(
                            root.get("createdAt"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(
                            root.get("createdAt"), to));
        }

        return auditLogRepository.findAll(spec, pageable)
                .map(AuditLogResponse::from);
    }
}
