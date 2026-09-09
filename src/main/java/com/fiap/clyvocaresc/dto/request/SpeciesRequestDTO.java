package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpeciesRequestDTO(
        @NotBlank(message = "name é obrigatório") @Size(max = 50) String name,
        @Size(max = 255) String description
) {}