package com.lumiora.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private Long id;

    private String name;

    private String code;

    private String email;

    private String phone;

    private String address;

    private boolean active;
}