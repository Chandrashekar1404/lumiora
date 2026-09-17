package com.lumiora.repository;

import com.lumiora.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;
import java.util.Optional;

import com.lumiora.entity.auth.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findAllByOrganization_Id(Long organizationId);

    Optional<User> findByIdAndOrganization_Id(Long id, Long organizationId);
}