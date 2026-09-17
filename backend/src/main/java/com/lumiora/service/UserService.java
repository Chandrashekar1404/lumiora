package com.lumiora.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.entity.auth.User;


public interface UserService {

    User save(User user);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findAll();

    User update(User user);

    void deleteById(Long id);

    List<User> findAllByOrganizationId(Long organizationId);

    Optional<User> findByIdAndOrganizationId(Long id, Long organizationId);
}