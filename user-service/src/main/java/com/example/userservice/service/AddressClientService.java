package com.example.userservice.service;

import com.example.userservice.model.dto.AddressRequestDTO;
import com.example.userservice.model.dto.AddressResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AddressClientService {

    private final WebClient addressServiceWebClient;

    public AddressResponseDTO createAddress(AddressRequestDTO dto) {
        return addressServiceWebClient.post()
                .uri("")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(AddressResponseDTO.class)
                .block();
    }

    public AddressResponseDTO getAddress(Long id) {
        return addressServiceWebClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(AddressResponseDTO.class)
                .block();
    }
}