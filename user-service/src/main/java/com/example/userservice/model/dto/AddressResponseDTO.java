package com.example.userservice.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private Double latitude;
    private Double longitude;
}