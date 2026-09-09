package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.CatalogItemRequestDTO;
import com.fiap.clyvocaresc.dto.response.CatalogItemResponseDTO;
import com.fiap.clyvocaresc.entity.enums.CatalogItemType;
import com.fiap.clyvocaresc.service.CatalogItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe o catálogo unificado de vacinas e medicamentos. Escrita restrita a
 * VETERINARIAN/CLINIC_ADMIN — é conteúdo clínico, não deve ser editável pelo tutor.
 */
@RestController
@RequestMapping("/api/catalog-items")
@RequiredArgsConstructor
public class CatalogItemController {

    private final CatalogItemService catalogItemService;

    /** Lista o catálogo inteiro, ou filtra por tipo se o query param `type` for informado. */
    @GetMapping
    public List<CatalogItemResponseDTO> findAll(@RequestParam(required = false) CatalogItemType type) {
        return type != null ? catalogItemService.findByType(type) : catalogItemService.findAll();
    }

    /** Busca um item de catálogo por id. */
    @GetMapping("/{id}")
    public CatalogItemResponseDTO findById(@PathVariable Long id) {
        return catalogItemService.findById(id);
    }

    /** Cria um novo item de catálogo (vacina ou medicamento). */
    @PostMapping
    public ResponseEntity<CatalogItemResponseDTO> create(@Valid @RequestBody CatalogItemRequestDTO dto) {
        return ResponseEntity.status(201).body(catalogItemService.create(dto));
    }

    /** Atualiza um item de catálogo existente. */
    @PutMapping("/{id}")
    public CatalogItemResponseDTO update(@PathVariable Long id, @Valid @RequestBody CatalogItemRequestDTO dto) {
        return catalogItemService.update(id, dto);
    }

    /** Remove um item de catálogo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
