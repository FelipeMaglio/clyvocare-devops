package com.fiap.clyvocaresc.dto.request;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record VaccinationRequestDTO(
        @NotNull(message = "applicationDate é obrigatório") @PastOrPresent(message = "applicationDate não pode ser no futuro") LocalDate applicationDate,
        String batch,
        LocalDate expirationDate,
        @NotNull(message = "petId é obrigatório") Long petId,
        @NotNull(message = "catalogItemId é obrigatório") Long catalogItemId,
        Long appointmentId,
        Long veterinarianId
) {}