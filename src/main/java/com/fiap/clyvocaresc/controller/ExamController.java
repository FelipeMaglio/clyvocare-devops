package com.fiap.clyvocaresc.controller;


import com.fiap.clyvocaresc.dto.request.ExamRequestDTO;
import com.fiap.clyvocaresc.dto.response.ExamResponseDTO;
import com.fiap.clyvocaresc.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe o registro de exames, complementando o histórico longitudinal do pet. */
@RestController
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /** Lista o histórico de exames de um pet. */
    @GetMapping("/api/pets/{petId}/exams")
    public List<ExamResponseDTO> findByPet(@PathVariable Long petId) {
        return examService.findByPet(petId);
    }

    /** Busca um exame específico por id. */
    @GetMapping("/api/exams/{id}")
    public ExamResponseDTO findById(@PathVariable Long id) {
        return examService.findById(id);
    }

    /** Registra um novo exame. */
    @PostMapping("/api/exams")
    public ResponseEntity<ExamResponseDTO> create(@Valid @RequestBody ExamRequestDTO dto) {
        return ResponseEntity.status(201).body(examService.create(dto));
    }

    /** Atualiza um exame, tipicamente pra preencher o resultado depois da solicitação. */
    @PutMapping("/api/exams/{id}")
    public ExamResponseDTO update(@PathVariable Long id, @Valid @RequestBody ExamRequestDTO dto) {
        return examService.update(id, dto);
    }

    /** Remove um exame do histórico. */
    @DeleteMapping("/api/exams/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
