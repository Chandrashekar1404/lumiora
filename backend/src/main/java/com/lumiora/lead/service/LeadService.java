package com.lumiora.lead.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.lead.entity.Lead;

public interface LeadService {

    Lead save(Lead lead);

    List<Lead> findAll();

    Optional<Lead> findById(Long id);

    List<Lead> findAllByOrganizationId(Long organizationId);

    Optional<Lead> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<Lead> findAllByOrganizationAndCounselor(
            Long organizationId,
            Long counselorId
    );

    Optional<Lead> findByIdAndOrganizationAndCounselor(
            Long id,
            Long organizationId,
            Long counselorId
    );

    boolean existsByEmailAndOrganizationId(
            String email,
            Long organizationId
    );

    boolean existsByPhoneAndOrganizationId(
            String phone,
            Long organizationId
    );
}