package com.fiap.clyvocaresc.dto.request;


import com.fiap.clyvocaresc.entity.enums.CatalogItemType;
import jakarta.validation.constraints.*;

public record CatalogItemRequestDTO(
        @NotBlank(message = "name é obrigatório") String name,
        String manufacturer,
        @NotNull(message = "type é obrigatório") CatalogItemType type,
        String activeIngredient,       // usado quando type = MEDICATION
        String diseasesPrevented,      // usado quando type = VACCINE
        @Positive(message = "boosterIntervalDays deve ser positivo") Integer boosterIntervalDays,
        Long speciesId
) {}
