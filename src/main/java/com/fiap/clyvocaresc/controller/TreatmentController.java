package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.TreatmentRequestDTO;
import com.fiap.clyvocaresc.dto.response.TreatmentResponseDTO;
import com.fiap.clyvocaresc.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe planos terapêuticos, com transições de status dedicadas (complete/suspend). */
@RestController
@RequiredArgsConstructor
public class TreatmentController {

    private final TreatmentService treatmentService;

    /** Lista o histórico de tratamentos de um pet. */
    @GetMapping("/api/pets/{petId}/treatments")
    public List<TreatmentResponseDTO> findByPet(@PathVariable Long petId) {
        return treatmentService.findByPet(petId);
    }

    /** Inicia um novo tratamento (status inicial ACTIVE). */
    @PostMapping("/api/treatments")
    public ResponseEntity<TreatmentResponseDTO> create(@Valid @RequestBody TreatmentRequestDTO dto) {
        return ResponseEntity.status(201).body(treatmentService.create(dto));
    }

    /** Atualiza descrição/datas de um tratamento existente. */
    @PutMapping("/api/treatments/{id}")
    public TreatmentResponseDTO update(@PathVariable Long id, @Valid @RequestBody TreatmentRequestDTO dto) {
        return treatmentService.update(id, dto);
    }

    /** Marca o tratamento como concluído. */
    @PatchMapping("/api/treatments/{id}/complete")
    public TreatmentResponseDTO complete(@PathVariable Long id) {
        return treatmentService.complete(id);
    }

    /** Marca o tratamento como suspenso. */
    @PatchMapping("/api/treatments/{id}/suspend")
    public TreatmentResponseDTO suspend(@PathVariable Long id) {
        return treatmentService.suspend(id);
    }
}
