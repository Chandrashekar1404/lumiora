package com.lumiora.service;

import com.lumiora.entity.auth.User;

import java.util.Optional;

public interface UserService {

    User save(User user);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}