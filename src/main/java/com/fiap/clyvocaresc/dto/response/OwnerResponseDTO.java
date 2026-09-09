package com.fiap.clyvocaresc.dto.response;

import java.time.LocalDate;

public record OwnerResponseDTO(
        Long id,
        String name,
        String phone,
        String document,
        LocalDate registeredAt,
        String cityName,
        int petCount
) {}