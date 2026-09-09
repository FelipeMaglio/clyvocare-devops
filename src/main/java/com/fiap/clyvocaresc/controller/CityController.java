package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.CityRequestDTO;
import com.fiap.clyvocaresc.dto.response.CityResponseDTO;
import com.fiap.clyvocaresc.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe o CRUD de cidades. Rotas de escrita restritas a perfis administrativos (regra na Security). */
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    /** Lista todas as cidades cadastradas. */
    @GetMapping
    public List<CityResponseDTO> findAll() {
        return cityService.findAll();
    }

    /** Busca uma cidade por id. */
    @GetMapping("/{id}")
    public CityResponseDTO findById(@PathVariable Long id) {
        return cityService.findById(id);
    }

    /** Cria uma nova cidade. */
    @PostMapping
    public ResponseEntity<CityResponseDTO> create(@Valid @RequestBody CityRequestDTO dto) {
        return ResponseEntity.status(201).body(cityService.create(dto));
    }

    /** Atualiza uma cidade existente. */
    @PutMapping("/{id}")
    public CityResponseDTO update(@PathVariable Long id, @Valid @RequestBody CityRequestDTO dto) {
        return cityService.update(id, dto);
    }

    /** Remove uma cidade. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
