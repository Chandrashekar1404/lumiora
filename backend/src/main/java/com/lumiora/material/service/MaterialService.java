package com.lumiora.material.service;

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.repository.BatchRepository;
import com.lumiora.course.entity.Course;
import com.lumiora.course.repository.CourseRepository;
import com.lumiora.enrollment.entity.Enrollment;
import com.lumiora.enrollment.entity.EnrollmentStatus;
import com.lumiora.enrollment.repository.EnrollmentRepository;
import com.lumiora.entity.auth.User;
import com.lumiora.material.dto.MaterialCreateRequest;
import com.lumiora.material.dto.MaterialResponse;
import com.lumiora.material.dto.MaterialUpdateRequest;
import com.lumiora.material.entity.Material;
import com.lumiora.material.repository.MaterialRepository;
import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;
import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final EnrollmentRepository enrollmentRepository;

    private User currentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Authenticated user not found"));
    }

    private String role(User user) {
        return user.getRole().getName().replace("ROLE_", "");
    }

    private Long organizationId(User user) {
        return user.getOrganization() == null
                ? null
                : user.getOrganization().getId();
    }

    private boolean isSuperAdmin(User user) {
        return "SUPER_ADMIN".equals(role(user));
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(role(user));
    }

    private boolean isTrainer(User user) {
        return "TRAINER".equals(role(user));
    }

    private boolean isStudent(User user) {
        return "STUDENT".equals(role(user));
    }

    private boolean sameId(Long first, Long second) {
        return first != null && second != null && first.equals(second);
    }

    private void validateUrl(String value) {
        try {
            URI uri = URI.create(value.trim());

            if (!("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException(
                        "Resource URL must be a valid HTTP or HTTPS URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Resource URL must be a valid HTTP or HTTPS URL");
        }
    }

    private boolean trainerAssignedToBatch(User user, Batch batch) {
        return batch.getTrainer() != null
                && sameId(batch.getTrainer().getId(), user.getId());
    }

    private boolean studentEnrolledInBatch(User user, Batch batch) {
        Long orgId = organizationId(user);

        if (orgId == null || batch == null) {
            return false;
        }

        return enrollmentRepository
                .findAllByOrganization_IdAndStudent_Id(orgId, user.getId())
                .stream()
                .anyMatch(enrollment ->
                        enrollment.getBatch() != null
                                && sameId(enrollment.getBatch().getId(), batch.getId())
                                && enrollment.getStatus() == EnrollmentStatus.ENROLLED);
    }

    private boolean canAccess(User user, Material material) {
        if (!material.isActive()) {
            return false;
        }

        if (isSuperAdmin(user)) {
            return true;
        }

        if (!sameId(organizationId(user), material.getOrganization().getId())) {
            return false;
        }

        if (isAdmin(user)) {
            return true;
        }

        if (isTrainer(user)) {
            if (material.getBatch() != null) {
                return trainerAssignedToBatch(user, material.getBatch());
            }

            return material.getCourse() != null
                    && batchRepository.findAll().stream()
                    .anyMatch(batch ->
                            batch.isActive()
                                    && batch.getCourse() != null
                                    && sameId(batch.getCourse().getId(),
                                              material.getCourse().getId())
                                    && trainerAssignedToBatch(user, batch));
        }

        if (isStudent(user)) {
            if (material.getBatch() != null) {
                return studentEnrolledInBatch(user, material.getBatch());
            }

            return material.getCourse() != null
                    && enrollmentRepository
                    .findAllByOrganization_IdAndStudent_Id(
                            organizationId(user), user.getId())
                    .stream()
                    .filter(enrollment ->
                            enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                    .anyMatch(enrollment ->
                            enrollment.getBatch() != null
                                    && enrollment.getBatch().getCourse() != null
                                    && sameId(
                                            enrollment.getBatch().getCourse().getId(),
                                            material.getCourse().getId()));
        }

        return false;
    }

    private void requireManageAccess(User user, Organization organization,
                                     Batch batch, Course course) {
        if (isSuperAdmin(user)) {
            return;
        }

        if (!sameId(organizationId(user), organization.getId())) {
            throw new IllegalArgumentException(
                    "You cannot manage materials for another organization");
        }

        if (isAdmin(user)) {
            return;
        }

        if (isTrainer(user)
                && batch != null
                && trainerAssignedToBatch(user, batch)) {
            return;
        }

        throw new IllegalArgumentException(
                "You do not have permission to manage this material");
    }

    private MaterialResponse toResponse(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getTitle(),
                material.getDescription(),
                material.getType(),
                material.getResourceUrl(),
                material.isActive(),
                material.getOrganization().getId(),
                material.getOrganization().getName(),
                material.getCourse() == null
                        ? null : material.getCourse().getId(),
                material.getCourse() == null
                        ? null : material.getCourse().getName(),
                material.getBatch() == null
                        ? null : material.getBatch().getId(),
                material.getBatch() == null
                        ? null : material.getBatch().getName(),
                material.getCreatedBy().getId(),
                material.getCreatedBy().getFirstName()
                        + " " + material.getCreatedBy().getLastName()
        );
    }

    public MaterialResponse create(MaterialCreateRequest request) {
        User user = currentUser();

        if (!(isSuperAdmin(user) || isAdmin(user) || isTrainer(user))) {
            throw new IllegalArgumentException(
                    "You do not have permission to create materials");
        }

        if (request.courseId() == null && request.batchId() == null) {
            throw new IllegalArgumentException(
                    "Provide at least a courseId or batchId");
        }

        validateUrl(request.resourceUrl());

        Organization organization = organizationRepository
                .findById(request.organizationId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found"));

        Course course = request.courseId() == null ? null
                : courseRepository.findById(request.courseId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found"));

        Batch batch = request.batchId() == null ? null
                : batchRepository.findById(request.batchId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Batch not found"));

        if (batch != null) {
            if (!sameId(batch.getOrganization().getId(), organization.getId())) {
                throw new IllegalArgumentException(
                        "Batch does not belong to the selected organization");
            }

            if (course == null) {
                course = batch.getCourse();
            } else if (batch.getCourse() == null
                    || !sameId(batch.getCourse().getId(), course.getId())) {
                throw new IllegalArgumentException(
                        "Course does not match the selected batch");
            }
        }

        if (course != null
                && !sameId(course.getOrganization().getId(), organization.getId())) {
            throw new IllegalArgumentException(
                    "Course does not belong to the selected organization");
        }

        requireManageAccess(user, organization, batch, course);

        Material material = new Material();
        material.setTitle(request.title().trim());
        material.setDescription(request.description());
        material.setType(request.type());
        material.setResourceUrl(request.resourceUrl().trim());
        material.setOrganization(organization);
        material.setCourse(course);
        material.setBatch(batch);
        material.setCreatedBy(user);
        material.setActive(true);

        return toResponse(materialRepository.save(material));
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getAll() {
        User user = currentUser();

        return materialRepository.findAll().stream()
                .filter(material -> canAccess(user, material))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMine() {
        return getAll();
    }

    @Transactional(readOnly = true)
    public MaterialResponse getById(Long id) {
        User user = currentUser();
        Material material = findMaterial(id);

        if (!canAccess(user, material)) {
            throw new IllegalArgumentException(
                    "Material not found or you do not have access");
        }

        return toResponse(material);
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getByBatch(Long batchId) {
        User user = currentUser();

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Batch not found"));

        if (isSuperAdmin(user)) {
            // Super Admin may inspect resources across organizations.
        } else if (!sameId(organizationId(user),
                          batch.getOrganization().getId())) {
            throw new IllegalArgumentException("You cannot access this batch");
        } else if (isTrainer(user) && !trainerAssignedToBatch(user, batch)) {
            throw new IllegalArgumentException("This batch is not assigned to you");
        } else if (isStudent(user) && !studentEnrolledInBatch(user, batch)) {
            throw new IllegalArgumentException("You are not enrolled in this batch");
        } else if (!(isSuperAdmin(user) || isAdmin(user)
                || isTrainer(user) || isStudent(user))) {
            throw new IllegalArgumentException("You cannot access this batch");
        }

        Long courseId = batch.getCourse() == null
                ? null : batch.getCourse().getId();

        return materialRepository.findAll().stream()
                .filter(Material::isActive)
                .filter(material ->
                        sameId(material.getOrganization().getId(),
                               batch.getOrganization().getId()))
                .filter(material ->
                        (material.getBatch() != null
                                && sameId(material.getBatch().getId(), batchId))
                        || (material.getBatch() == null
                                && material.getCourse() != null
                                && sameId(material.getCourse().getId(), courseId)))
                .filter(material -> canAccess(user, material))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getByCourse(Long courseId) {
        User user = currentUser();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found"));

        if (!isSuperAdmin(user)
                && !sameId(organizationId(user), course.getOrganization().getId())) {
            throw new IllegalArgumentException("You cannot access this course");
        }

        return materialRepository.findAll().stream()
                .filter(Material::isActive)
                .filter(material ->
                        sameId(material.getOrganization().getId(),
                               course.getOrganization().getId()))
                .filter(material ->
                        (material.getCourse() != null
                                && sameId(material.getCourse().getId(), courseId))
                        || (material.getBatch() != null
                                && material.getBatch().getCourse() != null
                                && sameId(material.getBatch().getCourse().getId(),
                                          courseId)))
                .filter(material -> canAccess(user, material))
                .map(this::toResponse)
                .toList();
    }

    public MaterialResponse update(Long id, MaterialUpdateRequest request) {
        User user = currentUser();
        Material material = findMaterial(id);

        requireManageAccess(user, material.getOrganization(),
                material.getBatch(), material.getCourse());

        if (request.title() != null) {
            if (request.title().isBlank() || request.title().length() > 150) {
                throw new IllegalArgumentException("Invalid material title");
            }
            material.setTitle(request.title().trim());
        }

        if (request.description() != null) {
            material.setDescription(request.description());
        }

        if (request.type() != null) {
            material.setType(request.type());
        }

        if (request.resourceUrl() != null) {
            validateUrl(request.resourceUrl());
            material.setResourceUrl(request.resourceUrl().trim());
        }

        return toResponse(materialRepository.save(material));
    }

    public void deactivate(Long id) {
        User user = currentUser();
        Material material = findMaterial(id);

        requireManageAccess(user, material.getOrganization(),
                material.getBatch(), material.getCourse());

        material.setActive(false);
        materialRepository.save(material);
    }

    private Material findMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Material not found"));
    }
}