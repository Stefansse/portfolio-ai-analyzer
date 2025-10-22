package com.example.userservice.model.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String fullName;

    private AddressRequestDTO address;
}
