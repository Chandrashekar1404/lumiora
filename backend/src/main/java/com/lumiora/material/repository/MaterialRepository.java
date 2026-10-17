package com.lumiora.material.repository;

import com.lumiora.material.entity.Material;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "organization", "course", "batch", "batch.course", "createdBy"
    })
    List<Material> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "organization", "course", "batch", "batch.course", "createdBy"
    })
    Optional<Material> findById(Long id);
}