package com.lumiora.batch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lumiora.batch.entity.Batch;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "course",
            "trainer"
    })
    List<Batch> findAll();

    @EntityGraph(attributePaths = {
            "organization",
            "course",
            "trainer"
    })
    Optional<Batch> findById(Long id);

    @EntityGraph(attributePaths = {
            "organization",
            "course",
            "trainer"
    })
    List<Batch> findAllByOrganization_Id(Long organizationId);

    @EntityGraph(attributePaths = {
            "organization",
            "course",
            "trainer"
    })
    Optional<Batch> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    boolean existsByCodeAndOrganization_Id(
            String code,
            Long organizationId
    );
}