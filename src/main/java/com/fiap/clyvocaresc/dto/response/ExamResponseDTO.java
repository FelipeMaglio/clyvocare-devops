package com.fiap.clyvocaresc.dto.response;


import java.time.LocalDate;

public record ExamResponseDTO(
        Long id,
        String examType,
        LocalDate requestDate,
        LocalDate resultDate,
        String result,
        String fileUrl,
        String laboratory,
        Long petId,
        String petName
) {}