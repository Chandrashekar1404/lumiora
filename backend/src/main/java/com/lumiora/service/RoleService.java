package com.lumiora.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.entity.auth.Role;

public interface RoleService {

    List<Role> getAllRoles();

    Role save(Role role);

    boolean existsByName(String roleName);

    Optional<Role> findByName(String roleName);
}