package com.fiap.clyvocaresc.dto.response;


import com.fiap.clyvocaresc.entity.enums.PetSex;
import com.fiap.clyvocaresc.entity.enums.PetSize;

import java.time.LocalDate;

public record PetResponseDTO(
        Long id,
        String name,
        LocalDate birthDate,
        PetSex sex,
        Double currentWeight,
        String microchip,
        String breed,
        PetSize size,
        String photoUrl,
        LocalDate registeredAt,
        Long ownerId,
        String ownerName,
        String speciesName
) {}