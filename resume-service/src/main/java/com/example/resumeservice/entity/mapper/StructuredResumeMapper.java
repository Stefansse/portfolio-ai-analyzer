package com.example.resumeservice.entity.mapper;

import com.example.resumeservice.entity.StructuredResume;
import com.example.resumeservice.entity.dto.StructuredResumeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StructuredResumeMapper {
    StructuredResumeDTO toDTO(StructuredResume structuredResume);
}