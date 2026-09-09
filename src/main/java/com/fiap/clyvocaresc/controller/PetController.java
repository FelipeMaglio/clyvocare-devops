package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.PetRequestDTO;
import com.fiap.clyvocaresc.dto.response.PetResponseDTO;
import com.fiap.clyvocaresc.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe o cadastro do Pet, entidade central do histórico longitudinal. A listagem
 * fica aninhada em `/owners/{ownerId}/pets` (não `/pets?ownerId=`) porque semanticamente
 * "os pets de um tutor" é um recurso filho de Owner, deixando a URL autoexplicativa.
 */
@RestController
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /** Lista os pets de um tutor específico. */
    @GetMapping("/api/owners/{ownerId}/pets")
    public List<PetResponseDTO> findByOwner(@PathVariable Long ownerId) {
        return petService.findByOwner(ownerId);
    }

    /** Busca a ficha de um pet específico por id. */
    @GetMapping("/api/pets/{id}")
    public PetResponseDTO findById(@PathVariable Long id) {
        return petService.findById(id);
    }

    /** Cadastra um novo pet. */
    @PostMapping("/api/pets")
    public ResponseEntity<PetResponseDTO> create(@Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.status(201).body(petService.create(dto));
    }

    /** Atualiza os dados de um pet existente. */
    @PutMapping("/api/pets/{id}")
    public PetResponseDTO update(@PathVariable Long id, @Valid @RequestBody PetRequestDTO dto) {
        return petService.update(id, dto);
    }

    /** Remove um pet do cadastro. */
    @DeleteMapping("/api/pets/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
