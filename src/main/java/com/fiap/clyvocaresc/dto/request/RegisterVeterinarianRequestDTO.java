package com.fiap.clyvocaresc.dto.request;
import jakarta.validation.constraints.*;

public record RegisterVeterinarianRequestDTO(
        @NotBlank(message = "username é obrigatório") @Size(min = 4, max = 150) String username,
        @NotBlank(message = "password é obrigatório") @Size(min = 6, message = "password deve ter no mínimo 6 caracteres") String password,
        @NotBlank(message = "name é obrigatório") String name,
        @NotBlank(message = "licenseNumber é obrigatório") String licenseNumber,
        String specialty,
        Long clinicId
) {}
