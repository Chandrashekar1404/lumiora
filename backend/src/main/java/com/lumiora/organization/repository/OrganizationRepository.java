package com.lumiora.organization.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.organization.entity.Organization;

public interface OrganizationRepository
        extends JpaRepository<Organization, Long> {

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Optional<Organization> findByCode(String code);
}