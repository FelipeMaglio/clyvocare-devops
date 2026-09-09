package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.SpeciesRequestDTO;
import com.fiap.clyvocaresc.dto.response.SpeciesResponseDTO;
import com.fiap.clyvocaresc.service.SpeciesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe o CRUD de espécies. Rotas de escrita restritas a perfis administrativos. */
@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class SpeciesController {

    private final SpeciesService speciesService;

    /** Lista todas as espécies cadastradas. */
    @GetMapping
    public List<SpeciesResponseDTO> findAll() {
        return speciesService.findAll();
    }

    /** Busca uma espécie por id. */
    @GetMapping("/{id}")
    public SpeciesResponseDTO findById(@PathVariable Long id) {
        return speciesService.findById(id);
    }

    /** Cria uma nova espécie. */
    @PostMapping
    public ResponseEntity<SpeciesResponseDTO> create(@Valid @RequestBody SpeciesRequestDTO dto) {
        return ResponseEntity.status(201).body(speciesService.create(dto));
    }

    /** Atualiza uma espécie existente. */
    @PutMapping("/{id}")
    public SpeciesResponseDTO update(@PathVariable Long id, @Valid @RequestBody SpeciesRequestDTO dto) {
        return speciesService.update(id, dto);
    }

    /** Remove uma espécie. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        speciesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
