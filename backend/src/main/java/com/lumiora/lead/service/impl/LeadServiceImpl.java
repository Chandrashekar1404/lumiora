package com.lumiora.lead.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.lead.entity.Lead;
import com.lumiora.lead.repository.LeadRepository;
import com.lumiora.lead.service.LeadService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;

    @Override
    public Lead save(Lead lead) {
        return leadRepository.save(lead);
    }

    @Override
    public List<Lead> findAll() {
        return leadRepository.findAll();
    }

    @Override
    public Optional<Lead> findById(Long id) {
        return leadRepository.findById(id);
    }

    @Override
    public List<Lead> findAllByOrganizationId(Long organizationId) {
        return leadRepository.findAllByOrganization_Id(
                organizationId
        );
    }

    @Override
    public Optional<Lead> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return leadRepository.findByIdAndOrganization_Id(
                id,
                organizationId
        );
    }

    @Override
    public List<Lead> findAllByOrganizationAndCounselor(
            Long organizationId,
            Long counselorId) {

        return leadRepository
                .findAllByOrganization_IdAndCounselor_Id(
                        organizationId,
                        counselorId
                );
    }

    @Override
    public Optional<Lead> findByIdAndOrganizationAndCounselor(
            Long id,
            Long organizationId,
            Long counselorId) {

        return leadRepository
                .findByIdAndOrganization_IdAndCounselor_Id(
                        id,
                        organizationId,
                        counselorId
                );
    }

    @Override
    public boolean existsByEmailAndOrganizationId(
            String email,
            Long organizationId) {

        return leadRepository
                .existsByEmailAndOrganization_Id(
                        email,
                        organizationId
                );
    }

    @Override
    public boolean existsByPhoneAndOrganizationId(
            String phone,
            Long organizationId) {

        return leadRepository
                .existsByPhoneAndOrganization_Id(
                        phone,
                        organizationId
                );
    }
}