package com.lumiora.batch.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.batch.entity.Batch;

public interface BatchService {

    Batch save(Batch batch);

    List<Batch> findAll();

    Optional<Batch> findById(Long id);

    List<Batch> findAllByOrganizationId(Long organizationId);

    Optional<Batch> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByCodeAndOrganizationId(
            String code,
            Long organizationId
    );
}