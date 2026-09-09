package com.fiap.clyvocaresc.dto.response;

import java.time.LocalDate;

public record VaccinationResponseDTO(
        Long id,
        LocalDate applicationDate,
        String batch,
        LocalDate nextDoseDate,   // calculado automaticamente no service (não vem do Request!)
        LocalDate expirationDate,
        Long petId,
        String petName,
        String vaccineName,
        String veterinarianName
) {}