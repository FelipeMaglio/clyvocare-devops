package com.fiap.clyvocaresc.dto.request;


import com.fiap.clyvocaresc.entity.enums.AppointmentType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AppointmentCreateRequestDTO(
        @NotNull(message = "appointmentDate é obrigatório") @FutureOrPresent(message = "appointmentDate não pode ser no passado") LocalDate appointmentDate,
        @NotNull(message = "type é obrigatório") AppointmentType type,
        @NotBlank(message = "reason é obrigatório") @Size(max = 500) String reason,
        @NotNull(message = "petId é obrigatório") Long petId,
        Long veterinarianId,
        Long clinicId
) {}
