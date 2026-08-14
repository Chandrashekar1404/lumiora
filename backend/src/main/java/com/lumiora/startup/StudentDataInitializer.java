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
@Order(3)
@RequiredArgsConstructor
public class StudentDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Value("${lumiora.bootstrap.student.email}")
    private String studentEmail;

    @Value("${lumiora.bootstrap.student.password}")
    private String studentPassword;

    @Value("${lumiora.bootstrap.student.first-name}")
    private String studentFirstName;

    @Value("${lumiora.bootstrap.student.last-name}")
    private String studentLastName;

    @Value("${lumiora.bootstrap.student.phone}")
    private String studentPhone;

    @Override
    public void run(String... args) {

        System.out.println("Checking Student...");

        if (userService.existsByEmail(studentEmail)) {

            System.out.println(
                    "✔ Student Already Exists : " + studentEmail
            );

            return;
        }

        Role studentRole = roleService
                .findByName("STUDENT")
                .orElseThrow(() -> new IllegalStateException(
                        "STUDENT role was not found. Role initialization failed."
                ));

        User student = new User();

        student.setEmail(studentEmail);
        student.setPhone(studentPhone);
        student.setPassword(
                passwordEncoder.encode(studentPassword)
        );
        student.setFirstName(studentFirstName);
        student.setLastName(studentLastName);
        student.setStatus(UserStatus.ACTIVE);
        student.setRole(studentRole);

        userService.save(student);

        System.out.println(
                "✔ Student Created : " + studentEmail
        );
    }
}