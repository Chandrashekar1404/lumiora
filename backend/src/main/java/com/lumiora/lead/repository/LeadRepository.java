package com.lumiora.lead.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.lead.entity.Lead;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "interestedCourse",
            "counselor"
    })
    List<Lead> findAll();

    @EntityGraph(attributePaths = {
            "organization",
            "interestedCourse",
            "counselor"
    })
    Optional<Lead> findById(Long id);

    @EntityGraph(attributePaths = {
            "organization",
            "interestedCourse",
            "counselor"
    })
    List<Lead> findAllByOrganization_Id(Long organizationId);

    @EntityGraph(attributePaths = {
            "organization",
            "interestedCourse",
            "counselor"
    })
    Optional<Lead> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "interestedCourse",
            "counselor"
    })
    List<Lead> findAllByOrganization_IdAndCounselor_Id(
            Long organizationId,
            Long counselorId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "interestedCourse",
            "counselor"
    })
    Optional<Lead> findByIdAndOrganization_IdAndCounselor_Id(
            Long id,
            Long organizationId,
            Long counselorId
    );

    boolean existsByEmailAndOrganization_Id(
            String email,
            Long organizationId
    );

    boolean existsByPhoneAndOrganization_Id(
            String phone,
            Long organizationId
    );
}