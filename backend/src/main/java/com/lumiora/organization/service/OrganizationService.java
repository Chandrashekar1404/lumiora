package com.lumiora.organization.service;

import com.lumiora.organization.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationService {

    Organization save(Organization organization);

    Optional<Organization> findById(Long id);

    Optional<Organization> findByCode(String code);

    List<Organization> findAll();

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Organization update(Organization organization);
}
