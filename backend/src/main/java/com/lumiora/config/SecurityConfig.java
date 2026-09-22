package com.lumiora.config;

import com.lumiora.security.CustomUserDetailsService;
import com.lumiora.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;

        private final PasswordEncoder passwordEncoder;

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public AuthenticationProvider authenticationProvider() {

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

                provider.setPasswordEncoder(passwordEncoder);

                return provider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http

                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authenticationProvider(authenticationProvider())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers("/api/auth/**")
                                                .permitAll()

                                                .requestMatchers("/api/users")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                .requestMatchers("/api/users/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                .requestMatchers("/api/organizations/**")
                                                .hasRole("SUPER_ADMIN")

                                                .requestMatchers("/api/courses/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                .requestMatchers("/api/test/admin")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                .requestMatchers("/api/test/student")
                                                .hasRole("STUDENT")

                                                .requestMatchers("/api/courses/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                .requestMatchers("/api/batches/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                .requestMatchers("/api/attendance/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN", "TRAINER")

                                                .requestMatchers("/api/enrollments/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                                                .requestMatchers("/api/fees/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN", "ACCOUNTANT")

                                                .requestMatchers("/api/payments/**")
                                                .hasAnyRole("SUPER_ADMIN", "ADMIN", "ACCOUNTANT")

                                                .anyRequest()
                                                .authenticated());

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration configuration)
                        throws Exception {

                return configuration.getAuthenticationManager();
        }
}