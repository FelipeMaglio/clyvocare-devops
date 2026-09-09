package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "username é obrigatório") String username,
        @NotBlank(message = "password é obrigatório") String password
) {}
