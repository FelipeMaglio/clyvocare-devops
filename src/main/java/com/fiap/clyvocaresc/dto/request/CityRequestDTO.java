package com.fiap.clyvocaresc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CityRequestDTO (

        @NotBlank(message = "name é obrigatório") @Size(max = 100) String name,
        @NotBlank(message = "state é obrigatório") @Size(min = 2, max = 2, message = "state deve ter 2 letras (UF)") String state,
        @NotBlank(message = "region é obrigatório") String region
){}