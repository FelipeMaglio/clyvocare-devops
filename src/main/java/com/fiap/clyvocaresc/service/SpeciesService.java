package com.fiap.clyvocaresc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fiap.clyvocaresc.dto.request.SpeciesRequestDTO;
import com.fiap.clyvocaresc.dto.response.SpeciesResponseDTO;
import com.fiap.clyvocaresc.entity.Species;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * Gerencia o cadastro de espécies (cão, gato, etc.), usado por Pet e CatalogItem.
 * É a base do monitoramento preventivo: é a espécie do pet que determina quais
 * vacinas/protocolos do CatalogItem se aplicam a ele. Assim como City, é catálogo
 * puro — sem regra de negócio além de validação de formulário.
 * <p>
 * Endpoints necessários: GET /api/species, GET /api/species/{id}, POST /api/species,
 * PUT /api/species/{id}, DELETE /api/species/{id}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SpeciesService {

    private final SpeciesRepository speciesRepository;

    /** Lista todas as espécies cadastradas. */
    @Transactional(readOnly = true)
    public List<SpeciesResponseDTO> findAll() {
        return speciesRepository.findAll().stream().map(this::toResponse).toList();
    }

    /** Busca uma espécie por id; lança 404 se não existir. */
    @Transactional(readOnly = true)
    public SpeciesResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Cria uma nova espécie no catálogo. */
    public SpeciesResponseDTO create(SpeciesRequestDTO dto) {
        Species species = new Species();
        apply(species, dto);
        return toResponse(speciesRepository.save(species));
    }

    /** Atualiza os dados de uma espécie existente. */
    public SpeciesResponseDTO update(Long id, SpeciesRequestDTO dto) {
        Species species = getOrThrow(id);
        apply(species, dto);
        return toResponse(speciesRepository.save(species));
    }

    /** Remove uma espécie do catálogo. */
    public void delete(Long id) {
        speciesRepository.delete(getOrThrow(id));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    private Species getOrThrow(Long id) {
        return speciesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espécie não encontrada com id " + id));
    }

    /** Copia os campos do DTO pra entidade. */
    private void apply(Species species, SpeciesRequestDTO dto) {
        species.setName(dto.name());
        species.setDescription(dto.description());
    }

    /** Converte a entidade em DTO de saída. */
    private SpeciesResponseDTO toResponse(Species species) {
        return new SpeciesResponseDTO(species.getId(), species.getName(), species.getDescription());
    }

}
