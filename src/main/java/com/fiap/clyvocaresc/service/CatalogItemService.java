package com.fiap.clyvocaresc.service;


import com.fiap.clyvocaresc.dto.request.CatalogItemRequestDTO;
import com.fiap.clyvocaresc.dto.response.CatalogItemResponseDTO;
import com.fiap.clyvocaresc.entity.CatalogItem;
import com.fiap.clyvocaresc.entity.Species;
import com.fiap.clyvocaresc.entity.enums.CatalogItemType;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.CatalogItemRepository;
import com.fiap.clyvocaresc.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gerencia o catálogo unificado de vacinas e medicamentos (campo `type` diferencia
 * VACCINE de MEDICATION). É o motor do pilar de monitoramento preventivo: é daqui
 * que Vaccination lê o `boosterIntervalDays` pra calcular a próxima dose automaticamente,
 * e que Prescription lê o item pra registrar uma dosagem de tratamento. A regra de negócio
 * central é a coerência condicional de campos — um item VACCINE precisa ter intervalo
 * de reforço definido, senão o cálculo de próxima dose em Vaccination quebraria.
 * <p>
 * Endpoints necessários: GET /api/catalog-items, GET /api/catalog-items?type=VACCINE,
 * GET /api/catalog-items/{id}, POST /api/catalog-items, PUT /api/catalog-items/{id},
 * DELETE /api/catalog-items/{id} — cadastro restrito a VETERINARIAN/CLINIC_ADMIN.
 */
@Service
@RequiredArgsConstructor
public class CatalogItemService {

    private final CatalogItemRepository catalogItemRepository;
    private final SpeciesRepository speciesRepository;

    /** Lista todos os itens de catálogo, vacinas e medicamentos misturados. */
    @Transactional(readOnly = true)
    public List<CatalogItemResponseDTO> findAll() {
        return catalogItemRepository.findAll().stream().map(this::toResponse).toList();
    }

    /** Filtra o catálogo por tipo (ex: só vacinas), usado pela tela de aplicação de vacina no frontend. */
    @Transactional(readOnly = true)
    public List<CatalogItemResponseDTO> findByType(CatalogItemType type) {
        return catalogItemRepository.findAll().stream()
                .filter(item -> item.getType() == type)
                .map(this::toResponse)
                .toList();
    }

    /** Busca um item específico por id; lança 404 se não existir. */
    @Transactional(readOnly = true)
    public CatalogItemResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Cria um item de catálogo, validando antes a coerência entre `type` e os campos específicos. */
    @Transactional
    public CatalogItemResponseDTO create(CatalogItemRequestDTO dto) {
        validateTypeCoherence(dto);

        CatalogItem item = new CatalogItem();
        apply(item, dto);
        return toResponse(catalogItemRepository.save(item));
    }

    /** Atualiza um item existente, revalidando a coerência de tipo antes de salvar. */
    @Transactional
    public CatalogItemResponseDTO update(Long id, CatalogItemRequestDTO dto) {
        validateTypeCoherence(dto);

        CatalogItem item = getOrThrow(id);
        apply(item, dto);
        return toResponse(catalogItemRepository.save(item));
    }

    /** Remove um item do catálogo. */
    @Transactional
    public void delete(Long id) {
        catalogItemRepository.delete(getOrThrow(id));
    }

    /** Garante que um item VACCINE sempre tenha boosterIntervalDays, pré-requisito do cálculo de reforço. */
    @Transactional
    private void validateTypeCoherence(CatalogItemRequestDTO dto) {
        if (dto.type() == CatalogItemType.VACCINE && dto.boosterIntervalDays() == null) {
            throw new IllegalArgumentException(
                    "boosterIntervalDays é obrigatório para itens do tipo VACCINE (necessário pro cálculo de próxima dose)");
        }
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    @Transactional
    private CatalogItem getOrThrow(Long id) {
        return catalogItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de catálogo não encontrado com id " + id));
    }

    /** Copia os campos do DTO pra entidade, resolvendo a Species relacionada quando informada. */
    @Transactional
    private void apply(CatalogItem item, CatalogItemRequestDTO dto) {
        item.setName(dto.name());
        item.setManufacturer(dto.manufacturer());
        item.setType(dto.type());
        item.setActiveIngredient(dto.activeIngredient());
        item.setDiseasesPrevented(dto.diseasesPrevented());
        item.setBoosterIntervalDays(dto.boosterIntervalDays());

        if (dto.speciesId() != null) {
            Species species = speciesRepository.findById(dto.speciesId())
                    .orElseThrow(() -> new ResourceNotFoundException("Espécie não encontrada com id " + dto.speciesId()));
            item.setSpecies(species);
        } else {
            item.setSpecies(null);
        }
    }

    /** Converte a entidade em DTO de saída. */
    @Transactional
    private CatalogItemResponseDTO toResponse(CatalogItem item) {
        return new CatalogItemResponseDTO(
                item.getId(), item.getName(), item.getManufacturer(), item.getType(),
                item.getActiveIngredient(), item.getDiseasesPrevented(), item.getBoosterIntervalDays(),
                item.getSpecies() != null ? item.getSpecies().getName() : null
        );
    }
}
