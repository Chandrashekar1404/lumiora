package com.lumiora.organization.service;

import com.lumiora.organization.entity.Organization;
import com.lumiora.organization.repository.OrganizationRepository;
import com.lumiora.organization.service.OrganizationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class OrganizationServiceImpl implements OrganizationService{
    private final OrganizationRepository organizationRepository;

    @Override
    public Organization save(Organization organization) {
        return organizationRepository.save(organization);
    }

    @Override
    public Optional<Organization> findById(Long id) {
        return organizationRepository.findById(id);
    }

    @Override
    public Optional<Organization> findByCode(String code) {
        return organizationRepository.findByCode(code);
    }

    @Override
    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    @Override
    public boolean existsByName(String name) {
        return organizationRepository.existsByName(name);
    }

    @Override
    public boolean existsByCode(String code) {
        return organizationRepository.existsByCode(code);
    }

    @Override
    public Organization update(Organization organization) {
        return organizationRepository.save(organization);
    }
}
