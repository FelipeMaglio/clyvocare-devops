package com.fiap.clyvocaresc.dto.response;
import java.time.LocalDate;

public record ClinicResponseDTO
        (
        Long id,
        String name,
        String taxId,
        String phone,
        String email,
        String address,
        String subscriptionPlan,
        LocalDate subscriptionDate,
        String cityName
) {}
