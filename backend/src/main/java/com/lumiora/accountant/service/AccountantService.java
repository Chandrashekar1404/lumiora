package com.lumiora.accountant.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.entity.auth.User;
import com.lumiora.fee.entity.FeeAccount;
import com.lumiora.fee.entity.Payment;

public interface AccountantService {

    List<User> findAllAccountants();

    List<User> findAllAccountantsByOrganization(
            Long organizationId
    );

    Optional<User> findAccountantById(
            Long id
    );

    Optional<User> findAccountantByIdAndOrganization(
            Long id,
            Long organizationId
    );

    Optional<User> findByPhone(
            String phone
    );

    User save(User accountant);

    List<FeeAccount> findFeesByOrganization(
            Long organizationId
    );

    List<Payment> findPaymentsByOrganization(
            Long organizationId
    );
}