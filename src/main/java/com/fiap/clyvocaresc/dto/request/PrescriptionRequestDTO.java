package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.*;

public record PrescriptionRequestDTO(
        @NotBlank(message = "dosage é obrigatório") String dosage,
        @NotBlank(message = "frequency é obrigatório") String frequency,
        @Positive(message = "durationDays deve ser positivo") Integer durationDays,
        String instructions,
        @NotNull(message = "treatmentId é obrigatório") Long treatmentId,
        @NotNull(message = "catalogItemId é obrigatório") Long catalogItemId
) {}