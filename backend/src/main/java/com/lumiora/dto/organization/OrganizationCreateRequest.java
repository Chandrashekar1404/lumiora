package com.lumiora.dto.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreateRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    @NotBlank(message = "Organization code is required")
    private String code;

    @Email(message = "Invalid organization email")
    private String email;

    private String phone;

    private String address;
}