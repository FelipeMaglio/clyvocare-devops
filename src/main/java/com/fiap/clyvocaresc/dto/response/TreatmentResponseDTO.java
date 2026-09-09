package com.fiap.clyvocaresc.dto.response;

import com.fiap.clyvocaresc.entity.enums.TreatmentStatus;

import java.time.LocalDate;

public record TreatmentResponseDTO(
        Long id,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        TreatmentStatus status,
        Long petId,
        String petName
) {}