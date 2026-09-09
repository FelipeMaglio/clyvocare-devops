package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.ClinicRequestDTO;
import com.fiap.clyvocaresc.dto.response.ClinicResponseDTO;
import com.fiap.clyvocaresc.service.ClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe o CRUD de clínicas. Escrita restrita a CLINIC_ADMIN. */
@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    /** Lista todas as clínicas cadastradas. */
    @GetMapping
    public List<ClinicResponseDTO> findAll() {
        return clinicService.findAll();
    }

    /** Busca uma clínica por id. */
    @GetMapping("/{id}")
    public ClinicResponseDTO findById(@PathVariable Long id) {
        return clinicService.findById(id);
    }

    /** Cadastra uma nova clínica. */
    @PostMapping
    public ResponseEntity<ClinicResponseDTO> create(@Valid @RequestBody ClinicRequestDTO dto) {
        return ResponseEntity.status(201).body(clinicService.create(dto));
    }

    /** Atualiza uma clínica existente. */
    @PutMapping("/{id}")
    public ClinicResponseDTO update(@PathVariable Long id, @Valid @RequestBody ClinicRequestDTO dto) {
        return clinicService.update(id, dto);
    }

    /** Remove uma clínica. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clinicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
