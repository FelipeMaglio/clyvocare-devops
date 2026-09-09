package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.VaccinationRequestDTO;
import com.fiap.clyvocaresc.dto.response.VaccinationResponseDTO;
import com.fiap.clyvocaresc.service.VaccinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller do fluxo de vacinação, o segundo fluxo completo do sistema. Um único
 * POST dispara toda a inteligência de negócio: validação de tipo, cálculo de
 * próxima dose e criação automática do Reminder de reforço — por isso não existe
 * PUT/DELETE aqui, é um evento histórico, não um cadastro editável.
 */
@RestController
@RequiredArgsConstructor
public class VaccinationController {

    private final VaccinationService vaccinationService;

    /** Lista o histórico de vacinação de um pet. */
    @GetMapping("/api/pets/{petId}/vaccinations")
    public List<VaccinationResponseDTO> findByPet(@PathVariable Long petId) {
        return vaccinationService.findByPet(petId);
    }

    /** Registra a aplicação de uma vacina — calcula nextDoseDate e cria o Reminder automaticamente. */
    @PostMapping("/api/vaccinations")
    public ResponseEntity<VaccinationResponseDTO> apply(@Valid @RequestBody VaccinationRequestDTO dto) {
        return ResponseEntity.status(201).body(vaccinationService.apply(dto));
    }
}
