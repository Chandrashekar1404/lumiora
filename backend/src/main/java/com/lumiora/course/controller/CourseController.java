package com.lumiora.course.controller;

import com.lumiora.course.dto.CourseCreateRequest;
import com.lumiora.course.dto.CourseResponse;
import com.lumiora.course.dto.CourseUpdateRequest;
import com.lumiora.course.entity.Course;
import com.lumiora.course.service.CourseService;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.entity.auth.User;

import com.lumiora.exception.UserNotFoundException;

import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;

import com.lumiora.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    private final UserService userService;

    private final OrganizationRepository organizationRepository;


    // =========================================================
    // CREATE COURSE
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            Authentication authentication) {

        boolean isSuperAdmin = isSuperAdmin(authentication);

        // Prevent duplicate code inside the organization
        if (courseService.existsByCodeAndOrganizationId(
                request.getCode(),
                request.getOrganizationId())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.failure(
                                    "A course with this code already exists in this organization"
                            )
                    );
        }

        Organization organization;

        if (isSuperAdmin) {

            // SUPER_ADMIN can create course for any organization
            organization = organizationRepository
                    .findById(request.getOrganizationId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Organization not found: "
                                            + request.getOrganizationId()
                            )
                    );

        } else {

            // ADMIN can only create course in own organization
            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {
                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long currentOrganizationId =
                    currentUser.getOrganization().getId();

            if (!currentOrganizationId.equals(
                    request.getOrganizationId())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                ApiResponse.failure(
                                        "You cannot create a course for another organization"
                                )
                        );
            }

            organization = currentUser.getOrganization();
        }

        Course course = new Course();

        course.setName(request.getName());
        course.setCode(request.getCode());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        course.setFee(request.getFee());
        course.setActive(true);
        course.setOrganization(organization);

        Course savedCourse = courseService.save(course);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Course created successfully",
                                toResponse(savedCourse)
                        )
                );
    }


    // =========================================================
    // GET ALL COURSES
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses(
            Authentication authentication) {

        List<Course> courses;

        if (isSuperAdmin(authentication)) {

            courses = courseService.findAllByOrganizationId(-1L);

            /*
             * We cannot use findAllByOrganizationId() for SUPER_ADMIN
             * because SUPER_ADMIN must see courses from every organization.
             */
            courses = courseService.findAllByOrganizationId(0L);

            /*
             * This will be replaced below after adding a global
             * findAll() method to CourseService.
             */
        }

        else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {
                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            courses = courseService.findAllByOrganizationId(
                    organizationId
            );
        }

        List<CourseResponse> responses = courses.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Courses fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // GET COURSE BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(
            @PathVariable Long id,
            Authentication authentication) {

        Course course;

        if (isSuperAdmin(authentication)) {

            course = courseService
                    .findByIdAndOrganizationId(id, getAnyOrganizationId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course not found with id: " + id
                            )
                    );

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {
                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            course = courseService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course not found with id: " + id
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Course fetched successfully",
                        toResponse(course)
                )
        );
    }


    // =========================================================
    // UPDATE COURSE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request,
            Authentication authentication) {

        Course course;

        if (isSuperAdmin(authentication)) {

            course = courseService
                    .findByIdAndOrganizationId(
                            id,
                            getAnyOrganizationId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course not found with id: " + id
                            )
                    );

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {
                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            course = courseService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course not found with id: " + id
                            )
                    );
        }

        if (request.getName() != null &&
                !request.getName().isBlank()) {

            course.setName(request.getName());
        }

        if (request.getDescription() != null) {

            course.setDescription(
                    request.getDescription()
            );
        }

        if (request.getDuration() != null) {

            course.setDuration(
                    request.getDuration()
            );
        }

        if (request.getFee() != null) {

            course.setFee(
                    request.getFee()
            );
        }

        if (request.getActive() != null) {

            course.setActive(
                    request.getActive()
            );
        }

        Course updatedCourse =
                courseService.save(course);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Course updated successfully",
                        toResponse(updatedCourse)
                )
        );
    }


    // =========================================================
    // DEACTIVATE COURSE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateCourse(
            @PathVariable Long id,
            Authentication authentication) {

        Course course;

        if (isSuperAdmin(authentication)) {

            course = courseService
                    .findByIdAndOrganizationId(
                            id,
                            getAnyOrganizationId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course not found with id: " + id
                            )
                    );

        } else {

            User currentUser = getCurrentUser(authentication);

            if (currentUser.getOrganization() == null) {
                throw new IllegalStateException(
                        "Admin is not assigned to an organization"
                );
            }

            Long organizationId =
                    currentUser.getOrganization().getId();

            course = courseService
                    .findByIdAndOrganizationId(
                            id,
                            organizationId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course not found with id: " + id
                            )
                    );
        }

        course.setActive(false);

        courseService.save(course);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Course deactivated successfully",
                        null
                )
        );
    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    private boolean isSuperAdmin(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_SUPER_ADMIN")
                );
    }


    private User getCurrentUser(
            Authentication authentication) {

        return userService
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Logged-in user not found"
                        )
                );
    }


    private CourseResponse toResponse(Course course) {

        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getCode(),
                course.getDescription(),
                course.getDuration(),
                course.getFee(),
                course.isActive(),
                course.getOrganization() != null
                        ? course.getOrganization().getId()
                        : null
        );
    }


    private Long getAnyOrganizationId() {

        return organizationRepository
                .findAll()
                .stream()
                .findFirst()
                .map(Organization::getId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No organization exists"
                        )
                );
    }
}