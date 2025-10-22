package com.example.userservice.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private String role;
    private boolean emailVerified;
    private Long addressId;// ✅ Add this
}