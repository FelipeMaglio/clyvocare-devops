package com.fiap.clyvocaresc.dto.response;

public record PrescriptionResponseDTO(
        Long id,
        String dosage,
        String frequency,
        Integer durationDays,
        String instructions,
        Long treatmentId,
        String medicationName
) {}