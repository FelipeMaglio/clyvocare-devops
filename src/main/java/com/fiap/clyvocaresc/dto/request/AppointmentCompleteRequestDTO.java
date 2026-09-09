package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AppointmentCompleteRequestDTO(@NotBlank(message = "diagnosis é obrigatório") @Size(max = 1000) String diagnosis,
                                            String notes,
                                            @Positive(message = "weightAtVisit deve ser positivo") Double weightAtVisit
) {}