package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.*;

public record ClinicRequestDTO(
        @NotBlank(message = "name é obrigatório") @Size(max = 150) String name,
        @NotBlank(message = "taxId é obrigatório") @Pattern(regexp = "\\d{14}", message = "taxId deve ter 14 dígitos (CNPJ)") String taxId,
        String phone,
        @Email(message = "email inválido") String email,
        String address,
        String subscriptionPlan,
        Long cityId
) {}
