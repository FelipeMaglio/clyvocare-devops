package com.fiap.clyvocaresc.dto.request;


import jakarta.validation.constraints.*;

public record VeterinarianUpdateRequestDTO(
        @NotBlank(message = "name é obrigatório") String name,
        String specialty,
        @Email(message = "email inválido") String email,
        String phone,
        Long clinicId
) {}