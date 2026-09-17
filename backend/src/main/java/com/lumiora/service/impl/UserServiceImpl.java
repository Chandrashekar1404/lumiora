package com.lumiora.service.impl;

import com.lumiora.entity.auth.User;
import com.lumiora.repository.UserRepository;
import com.lumiora.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import java.util.List;

import java.util.List;
import java.util.Optional;

import com.lumiora.entity.auth.User;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findAllByOrganizationId(Long organizationId) {
        return userRepository.findAllByOrganization_Id(organizationId);
    }

    @Override
    public Optional<User> findByIdAndOrganizationId(
            Long id,
            Long organizationId) {

        return userRepository.findByIdAndOrganization_Id(
                id,
                organizationId);
    }


}