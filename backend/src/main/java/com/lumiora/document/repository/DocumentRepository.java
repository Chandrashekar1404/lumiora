package com.lumiora.document.repository;

import com.lumiora.document.entity.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = {
        "organization", "ownerUser", "uploadedBy"
    })
    List<Document> findAllByActiveTrue();

    @EntityGraph(attributePaths = {
        "organization", "ownerUser", "uploadedBy"
    })
    List<Document> findAllByOrganization_IdAndActiveTrue(Long organizationId);

    @EntityGraph(attributePaths = {
        "organization", "ownerUser", "uploadedBy"
    })
    List<Document> findAllByOwnerUser_IdAndActiveTrue(Long ownerUserId);

    @EntityGraph(attributePaths = {
        "organization", "ownerUser", "uploadedBy"
    })
    Optional<Document> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = {
        "organization", "ownerUser", "uploadedBy"
    })
    List<Document> findAllByOrganization_IdAndOwnerUser_IdAndActiveTrue(
            Long organizationId,
            Long ownerUserId
    );
}