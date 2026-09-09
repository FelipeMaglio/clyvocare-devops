package com.fiap.clyvocaresc.dto.request;

import com.fiap.clyvocaresc.entity.enums.PetSex;
import com.fiap.clyvocaresc.entity.enums.PetSize;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PetRequestDTO(
        @NotBlank(message = "name é obrigatório") @Size(max = 100) String name,
        @PastOrPresent(message = "birthDate não pode ser no futuro") LocalDate birthDate,
        PetSex sex,
        @PositiveOrZero(message = "currentWeight não pode ser negativo") Double currentWeight,
        String microchip,
        String breed,
        PetSize size,
        String photoUrl,
        @NotNull(message = "ownerId é obrigatório") Long ownerId,
        @NotNull(message = "speciesId é obrigatório") Long speciesId
) {}
