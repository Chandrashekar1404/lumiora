package com.lumiora.startup;

import com.lumiora.entity.auth.Role;
import com.lumiora.entity.auth.User;
import com.lumiora.entity.auth.UserStatus;
import com.lumiora.service.RoleService;
import com.lumiora.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class SuperAdminDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Value("${lumiora.bootstrap.admin.email}")
    private String adminEmail;

    @Value("${lumiora.bootstrap.admin.password}")
    private String adminPassword;

    @Value("${lumiora.bootstrap.admin.first-name}")
    private String adminFirstName;

    @Value("${lumiora.bootstrap.admin.last-name}")
    private String adminLastName;

    @Value("${lumiora.bootstrap.admin.phone}")
    private String adminPhone;

    @Override
    public void run(String... args) {

        System.out.println("Checking Super Admin...");

        if (userService.existsByEmail(adminEmail)) {

            System.out.println("✔ Super Admin Already Exists : " + adminEmail);
            return;
        }

        Role superAdminRole = roleService
                .findByName("SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "SUPER_ADMIN role was not found. Role initialization failed."
                ));

        User superAdmin = new User();

        superAdmin.setEmail(adminEmail);
        superAdmin.setPhone(adminPhone);
        superAdmin.setPassword(passwordEncoder.encode(adminPassword));
        superAdmin.setFirstName(adminFirstName);
        superAdmin.setLastName(adminLastName);
        superAdmin.setStatus(UserStatus.ACTIVE);
        superAdmin.setRole(superAdminRole);

        userService.save(superAdmin);

        System.out.println("✔ Super Admin Created : " + adminEmail);
    }
}