package com.example.userservice.model;

import com.example.userservice.model.enumeration.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String password;

    private String fullName;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isOauth2User;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;


    @Column(nullable = false)
    private boolean emailVerified = false;

    private String verificationCode;

    @Column(name = "address_id", nullable = true)
    private Long addressId;
}