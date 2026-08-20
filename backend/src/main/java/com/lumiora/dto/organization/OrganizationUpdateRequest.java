package com.lumiora.dto.organization;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUpdateRequest {

    private String name;

    @Email(message = "Invalid organization email")
    private String email;

    private String phone;

    private String address;
}