package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TreatmentRequestDTO(
        @NotBlank(message = "description é obrigatório") @Size(max = 500) String description,
        @NotNull(message = "startDate é obrigatório") LocalDate startDate,
        LocalDate endDate,
        @NotNull(message = "petId é obrigatório") Long petId,
        Long appointmentId
) {}