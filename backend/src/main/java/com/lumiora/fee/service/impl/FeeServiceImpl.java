package com.lumiora.fee.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.repository.FeeAccountRepository;
import com.lumiora.fee.service.FeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {

    private final FeeAccountRepository feeAccountRepository;

    @Override
    public FeeAccount save(FeeAccount feeAccount) {
        return feeAccountRepository.save(feeAccount);
    }

    @Override
    public List<FeeAccount> findAll() {
        return feeAccountRepository.findAll();
    }

    @Override
    public Optional<FeeAccount> findById(Long id) {
        return feeAccountRepository.findById(id);
    }

    @Override
    public List<FeeAccount> findAllByOrganizationId(
            Long organizationId) {

        return feeAccountRepository
                .findAllByOrganization_Id(organizationId);
    }

    @Override
    public Optional<FeeAccount> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return feeAccountRepository
                .findByIdAndOrganization_Id(
                        id,
                        organizationId
                );
    }

    @Override
    public boolean existsByEnrollmentAndOrganization(
            Long enrollmentId,
            Long organizationId) {

        return feeAccountRepository
                .existsByEnrollment_IdAndOrganization_Id(
                        enrollmentId,
                        organizationId
                );
    }

    @Override
    public Optional<FeeAccount> findByEnrollmentAndOrganization(
            Long enrollmentId,
            Long organizationId) {

        return feeAccountRepository
                .findByEnrollment_IdAndOrganization_Id(
                        enrollmentId,
                        organizationId
                );
    }
}