package com.example.userservice.model.mapper;

import com.example.userservice.model.User;
import com.example.userservice.model.dto.UserRegisterDTO;
import com.example.userservice.model.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Convert DTO to Entity
    User toEntity(UserRegisterDTO dto);

    // Convert Entity to Response DTO
    @Mapping(source = "emailVerified", target = "emailVerified")
    UserResponseDTO toDTO(User user);
}