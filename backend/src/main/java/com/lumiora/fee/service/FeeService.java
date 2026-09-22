package com.lumiora.fee.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.fee.entity.FeeAccount;

public interface FeeService {

    FeeAccount save(FeeAccount feeAccount);

    List<FeeAccount> findAll();

    Optional<FeeAccount> findById(Long id);

    List<FeeAccount> findAllByOrganizationId(
            Long organizationId
    );

    Optional<FeeAccount> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByEnrollmentAndOrganization(
            Long enrollmentId,
            Long organizationId
    );

    Optional<FeeAccount> findByEnrollmentAndOrganization(
            Long enrollmentId,
            Long organizationId
    );
}