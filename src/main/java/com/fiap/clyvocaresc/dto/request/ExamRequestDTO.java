package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ExamRequestDTO(
        @NotBlank(message = "examType é obrigatório") String examType,
        @NotNull(message = "requestDate é obrigatório") LocalDate requestDate,
        LocalDate resultDate,
        String result,
        String fileUrl,
        String laboratory,
        @NotNull(message = "petId é obrigatório") Long petId,
        Long appointmentId
) {}