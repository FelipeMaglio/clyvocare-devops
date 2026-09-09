package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.PrescriptionRequestDTO;
import com.fiap.clyvocaresc.dto.response.PrescriptionResponseDTO;
import com.fiap.clyvocaresc.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe a posologia (dosagem/frequência) de medicamentos dentro de um tratamento. */
@RestController
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /** Lista as prescrições de um tratamento específico. */
    @GetMapping("/api/treatments/{treatmentId}/prescriptions")
    public List<PrescriptionResponseDTO> findByTreatment(@PathVariable Long treatmentId) {
        return prescriptionService.findByTreatment(treatmentId);
    }

    /** Registra uma nova prescrição, validando que o item de catálogo é um medicamento. */
    @PostMapping("/api/prescriptions")
    public ResponseEntity<PrescriptionResponseDTO> create(@Valid @RequestBody PrescriptionRequestDTO dto) {
        return ResponseEntity.status(201).body(prescriptionService.create(dto));
    }
}
