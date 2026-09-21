package com.lumiora.batch.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.repository.BatchRepository;
import com.lumiora.batch.service.BatchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;

    @Override
    public Batch save(Batch batch) {
        return batchRepository.save(batch);
    }

    @Override
    public List<Batch> findAll() {
        return batchRepository.findAll();
    }

    @Override
    public Optional<Batch> findById(Long id) {
        return batchRepository.findById(id);
    }

    @Override
    public List<Batch> findAllByOrganizationId(Long organizationId) {
        return batchRepository.findAllByOrganization_Id(
                organizationId
        );
    }

    @Override
    public Optional<Batch> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return batchRepository.findByIdAndOrganization_Id(
                id,
                organizationId
        );
    }

    @Override
    public boolean existsByCodeAndOrganizationId(
            String code,
            Long organizationId) {

        return batchRepository.existsByCodeAndOrganization_Id(
                code,
                organizationId
        );
    }
}