package com.fiap.clyvocaresc.dto.response;


public record VeterinarianResponseDTO(
        Long id,
        String name,
        String licenseNumber,
        String specialty,
        String email,
        String phone,
        String clinicName
) {}