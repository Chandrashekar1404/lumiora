package com.lumiora.document.entity;

import com.lumiora.entity.auth.User;
import com.lumiora.entity.base.BaseEntity;
import com.lumiora.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "documents",
    indexes = {
        @Index(name = "idx_document_org_active",
               columnList = "organization_id, active"),
        @Index(name = "idx_document_owner_active",
               columnList = "owner_user_id, active")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_document_storage_key",
            columnNames = "storage_key"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Document extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DocumentCategory category = DocumentCategory.OTHER;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    // Random internal key; never use the original filename as a disk path.
    @Column(name = "storage_key", nullable = false, length = 100)
    private String storageKey;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;
}