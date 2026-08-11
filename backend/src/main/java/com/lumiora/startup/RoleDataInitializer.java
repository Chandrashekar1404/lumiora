package com.lumiora.startup;

import com.lumiora.common.constants.RoleConstants;
import com.lumiora.entity.auth.Role;
import com.lumiora.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleService roleService;

    @Override
    public void run(String... args) {

        List<String> defaultRoles = List.of(
                RoleConstants.SUPER_ADMIN,
                RoleConstants.ADMIN,
                RoleConstants.TRAINER,
                RoleConstants.STUDENT,
                RoleConstants.COUNSELOR,
                RoleConstants.ACCOUNTANT
        );

        for (String roleName : defaultRoles) {

            if (!roleService.existsByName(roleName)) {

                Role role = new Role();
                role.setName(roleName);
                role.setDescription(roleName + " Role");
                role.setActive(true);

                roleService.save(role);

                System.out.println("✔ Created Role : " + roleName);

            } else {

                System.out.println("✔ Role Already Exists : " + roleName);

            }

        }

    }
}