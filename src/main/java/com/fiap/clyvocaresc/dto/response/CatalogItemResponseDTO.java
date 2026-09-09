package com.fiap.clyvocaresc.dto.response;

import com.fiap.clyvocaresc.entity.enums.CatalogItemType;

public record CatalogItemResponseDTO(
        Long id,
        String name,
        String manufacturer,
        CatalogItemType type,
        String activeIngredient,
        String diseasesPrevented,
        Integer boosterIntervalDays,
        String speciesName
) {}