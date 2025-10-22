package com.example.resumeservice.entity.mapper;

import com.example.resumeservice.entity.Resume;
import com.example.resumeservice.entity.dto.ResumeResponseDTO;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ResumeMapper {

    ResumeResponseDTO toDTO(Resume resume);
}
