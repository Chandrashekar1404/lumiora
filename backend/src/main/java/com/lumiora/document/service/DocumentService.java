package com.lumiora.document.service;

import com.lumiora.document.dto.DocumentResponse;
import com.lumiora.document.entity.Document;
import com.lumiora.document.entity.DocumentCategory;
import com.lumiora.document.repository.DocumentRepository;
import com.lumiora.entity.auth.User;
import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;
import com.lumiora.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DocumentStorageService storageService;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            DocumentStorageService storageService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.storageService = storageService;
    }

    @Transactional
    public DocumentResponse upload(
            String title,
            String description,
            DocumentCategory category,
            Long organizationId,
            Long ownerUserId,
            MultipartFile file) {

        User actor = currentUser();
        String role = roleOf(actor);

        if (!List.of("SUPER_ADMIN", "ADMIN", "TRAINER", "STUDENT")
                .contains(role)) {
            throw forbidden("You are not allowed to upload documents");
        }

        validateTitle(title);
        validateFile(file);

        Organization organization =
                resolveOrganization(actor, organizationId);

        User owner = resolveOwner(actor, role, ownerUserId, organization);

        String storageKey = null;

        try {
            storageKey = storageService.store(file);

            Document document = new Document();
            document.setTitle(title.trim());
            document.setDescription(normalizeDescription(description));
            document.setCategory(category == null
                    ? DocumentCategory.OTHER : category);
            document.setOriginalFilename(safeOriginalFilename(
                    file.getOriginalFilename()));
            document.setContentType(detectContentType(file));
            document.setSizeBytes(file.getSize());
            document.setStorageKey(storageKey);
            document.setActive(true);
            document.setOrganization(organization);
            document.setOwnerUser(owner);
            document.setUploadedBy(actor);

            Document saved = documentRepository.save(document);
            return toResponse(saved);

        } catch (IOException | RuntimeException exception) {
            if (storageKey != null) {
                try {
                    storageService.delete(storageKey);
                } catch (IOException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }

            if (exception instanceof ResponseStatusException responseException) {
                throw responseException;
            }

            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store document");
        }
    }

    @Transactional
    public List<DocumentResponse> getAll() {
        User actor = currentUser();
        String role = roleOf(actor);

        List<Document> documents;

        switch (role) {
            case "SUPER_ADMIN" ->
                    documents = documentRepository.findAllByActiveTrue();

            case "ADMIN", "TRAINER" -> {
                Long orgId = organizationIdOf(actor);
                documents = documentRepository
                        .findAllByOrganization_IdAndActiveTrue(orgId);
            }

            case "STUDENT" ->
                    documents = documentRepository
                            .findAllByOwnerUser_IdAndActiveTrue(actor.getId());

            default -> throw forbidden(
                    "You are not allowed to view documents");
        }

        return documents.stream().map(this::toResponse).toList();
    }

    @Transactional
    public DocumentResponse getById(Long id) {
        User actor = currentUser();
        Document document = findActive(id);
        assertCanRead(actor, document);
        return toResponse(document);
    }

    @Transactional
    public List<DocumentResponse> getByStudent(Long studentUserId) {
        User actor = currentUser();
        String role = roleOf(actor);

        User student = userRepository.findById(studentUserId)
                .orElseThrow(() -> notFound("Student user not found"));

        if (!"STUDENT".equals(role)
                && !List.of("SUPER_ADMIN", "ADMIN", "TRAINER").contains(role)) {
            throw forbidden("You cannot view this student's documents");
        }

        if ("STUDENT".equals(role) && !actor.getId().equals(student.getId())) {
            throw forbidden("You can only view your own documents");
        }

        if (!"SUPER_ADMIN".equals(role)) {
            requireSameOrganization(actor, student);
        }

        return documentRepository
                .findAllByOrganization_IdAndOwnerUser_IdAndActiveTrue(
                        organizationIdOf(student), student.getId())
                .stream()
                .filter(document -> canRead(actor, document))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Resource download(Long id) {
        User actor = currentUser();
        Document document = findActive(id);
        assertCanRead(actor, document);

        try {
            return storageService.load(document.getStorageKey());
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Document file not found");
        }
    }

    @Transactional
    public DocumentResponse updateMetadata(
            Long id,
            String title,
            String description,
            DocumentCategory category) {

        User actor = currentUser();
        Document document = findActive(id);
        assertCanManage(actor, document);

        validateTitle(title);

        document.setTitle(title.trim());
        document.setDescription(normalizeDescription(description));

        if (category != null) {
            document.setCategory(category);
        }

        return toResponse(documentRepository.save(document));
    }

    @Transactional
    public void deactivate(Long id) {
        User actor = currentUser();
        Document document = findActive(id);
        assertCanManage(actor, document);

        // Soft delete: metadata is hidden; the physical file is retained.
        document.setActive(false);
        documentRepository.save(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Please select a file to upload");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Maximum file size is 10 MB");
        }

        String filename = safeOriginalFilename(file.getOriginalFilename())
                .toLowerCase(Locale.ROOT);

        if (!(filename.endsWith(".pdf")
                || filename.endsWith(".jpg")
                || filename.endsWith(".jpeg")
                || filename.endsWith(".png"))) {
            throw badRequest("Only PDF, JPG, JPEG and PNG files are allowed");
        }

        try {
            String actualType = detectContentType(file);

            boolean valid = switch (actualType) {
                case "application/pdf" ->
                        filename.endsWith(".pdf");
                case "image/jpeg" ->
                        filename.endsWith(".jpg")
                                || filename.endsWith(".jpeg");
                case "image/png" ->
                        filename.endsWith(".png");
                default -> false;
            };

            if (!valid) {
                throw badRequest(
                        "File content does not match its file extension");
            }
        } catch (IOException exception) {
            throw badRequest("Unable to validate uploaded file");
        }
    }

    private String detectContentType(MultipartFile file) throws IOException {
        byte[] header = new byte[8];

        try (InputStream input = file.getInputStream()) {
            int count = input.read(header);
            if (count < 4) {
                throw new IOException("File is too small");
            }
        }

        // PDF signature: %PDF
        if (header[0] == '%' && header[1] == 'P'
                && header[2] == 'D' && header[3] == 'F') {
            return "application/pdf";
        }

        // JPEG signature: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        // PNG signature
        byte[] pngSignature = {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };

        if (header.length == 8) {
            boolean png = true;
            for (int i = 0; i < pngSignature.length; i++) {
                if (header[i] != pngSignature[i]) {
                    png = false;
                    break;
                }
            }
            if (png) {
                return "image/png";
            }
        }

        throw new IOException("Unsupported file signature");
    }

    private Organization resolveOrganization(User actor, Long requestedId) {
        String role = roleOf(actor);
        Long actorOrgId = organizationIdOf(actor);

        Long targetId = requestedId != null ? requestedId : actorOrgId;

        if (targetId == null) {
            throw badRequest("An organization ID is required");
        }

        if (!"SUPER_ADMIN".equals(role)
                && !targetId.equals(actorOrgId)) {
            throw forbidden("You cannot access another organization");
        }

        return organizationRepository.findById(targetId)
                .orElseThrow(() -> notFound("Organization not found"));
    }

    private User resolveOwner(
            User actor,
            String role,
            Long ownerUserId,
            Organization organization) {

        if ("STUDENT".equals(role)) {
            if (ownerUserId != null
                    && !ownerUserId.equals(actor.getId())) {
                throw forbidden("Students can only upload their own documents");
            }
            return actor;
        }

        if (ownerUserId == null) {
            return null;
        }

        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> notFound("Document owner not found"));

        if (!"STUDENT".equals(roleOf(owner))) {
            throw badRequest("Document owner must be a student");
        }

        if (!organization.getId().equals(organizationIdOf(owner))) {
            throw badRequest("Student must belong to the selected organization");
        }

        return owner;
    }

    private Document findActive(Long id) {
        return documentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> notFound("Document not found"));
    }

    private void assertCanRead(User actor, Document document) {
        if (!canRead(actor, document)) {
            // Return 404 to avoid revealing another tenant's document.
            throw notFound("Document not found");
        }
    }

    private boolean canRead(User actor, Document document) {
        String role = roleOf(actor);

        if ("SUPER_ADMIN".equals(role)) {
            return true;
        }

        if (!organizationIdOf(actor).equals(
                document.getOrganization().getId())) {
            return false;
        }

        if ("ADMIN".equals(role) || "TRAINER".equals(role)) {
            return true;
        }

        return "STUDENT".equals(role)
                && document.getOwnerUser() != null
                && actor.getId().equals(document.getOwnerUser().getId());
    }

    private void assertCanManage(User actor, Document document) {
        String role = roleOf(actor);

        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        if (("ADMIN".equals(role) || "TRAINER".equals(role))
                && organizationIdOf(actor).equals(
                        document.getOrganization().getId())) {
            return;
        }

        throw forbidden("You cannot modify this document");
    }

    private void requireSameOrganization(User first, User second) {
        Long firstOrg = organizationIdOf(first);
        Long secondOrg = organizationIdOf(second);

        if (firstOrg == null || !firstOrg.equals(secondOrg)) {
            throw notFound("Student not found");
        }
    }

    private Long organizationIdOf(User user) {
        return user.getOrganization() == null
                ? null : user.getOrganization().getId();
    }

    private User currentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private String roleOf(User user) {
        if (user.getRole() == null || user.getRole().getName() == null) {
            throw forbidden("User role is not configured");
        }

        return user.getRole().getName()
                .replace("ROLE_", "")
                .toUpperCase(Locale.ROOT);
    }

    private String safeOriginalFilename(String filename) {
        if (filename == null) {
            return "document";
        }

        String normalized = filename.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1);
        normalized = normalized.replaceAll("[\\p{Cntrl}]", "_");

        if (normalized.length() > 255) {
            normalized = normalized.substring(normalized.length() - 255);
        }

        return normalized.isBlank() ? "document" : normalized;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw badRequest("Document title is required");
        }

        if (title.trim().length() > 150) {
            throw badRequest("Title cannot exceed 150 characters");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String normalized = description.trim();

        if (normalized.length() > 1000) {
            throw badRequest("Description cannot exceed 1000 characters");
        }

        return normalized;
    }

    private DocumentResponse toResponse(Document document) {
        User owner = document.getOwnerUser();
        User uploader = document.getUploadedBy();

        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getCategory(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.isActive(),
                document.getOrganization().getId(),
                owner == null ? null : owner.getId(),
                owner == null ? null :
                        (owner.getFirstName() + " " + owner.getLastName()).trim(),
                uploader.getId(),
                (uploader.getFirstName() + " " + uploader.getLastName()).trim()
        );
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}