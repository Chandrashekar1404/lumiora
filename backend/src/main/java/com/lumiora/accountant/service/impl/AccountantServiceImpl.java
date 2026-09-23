package com.lumiora.accountant.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.accountant.service.AccountantService;
import com.lumiora.entity.auth.User;
import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.Payment;
import com.lumiora.fee.service.FeeService;
import com.lumiora.fee.service.PaymentService;
import com.lumiora.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountantServiceImpl
        implements AccountantService {

    private final UserRepository userRepository;

    private final FeeService feeService;

    private final PaymentService paymentService;


    @Override
    public List<User> findAllAccountants() {

        return userRepository.findAllByRole_Name(
                "ACCOUNTANT"
        );
    }


    @Override
    public List<User> findAllAccountantsByOrganization(
            Long organizationId
    ) {

        return userRepository
                .findAllByOrganization_IdAndRole_Name(
                        organizationId,
                        "ACCOUNTANT"
                );
    }


    @Override
    public Optional<User> findAccountantById(
            Long id
    ) {

        return userRepository
                .findById(id)
                .filter(this::isAccountant);
    }


    @Override
    public Optional<User> findAccountantByIdAndOrganization(
            Long id,
            Long organizationId
    ) {

        return userRepository
                .findByIdAndOrganization_Id(
                        id,
                        organizationId
                )
                .filter(this::isAccountant);
    }


    @Override
    public Optional<User> findByPhone(
            String phone
    ) {

        return userRepository.findByPhone(phone);
    }


    @Override
    public User save(
            User accountant
    ) {

        return userRepository.save(accountant);
    }


    @Override
    public List<FeeAccount> findFeesByOrganization(
            Long organizationId
    ) {

        return feeService
                .findAllByOrganizationId(
                        organizationId
                );
    }


    @Override
    public List<Payment> findPaymentsByOrganization(
            Long organizationId
    ) {

        return paymentService
                .findAllByOrganizationId(
                        organizationId
                );
    }


    private boolean isAccountant(
            User user
    ) {

        return user.getRole() != null
                && "ACCOUNTANT".equals(
                        user.getRole().getName()
                );
    }
}