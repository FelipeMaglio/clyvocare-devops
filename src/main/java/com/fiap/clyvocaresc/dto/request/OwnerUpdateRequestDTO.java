package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OwnerUpdateRequestDTO(
        @NotBlank(message = "name é obrigatório") String name,
        @NotBlank(message = "phone é obrigatório") String phone,
        Long cityId
) {}