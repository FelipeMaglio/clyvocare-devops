package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.request.PetRequestDTO;
import com.fiap.clyvocaresc.dto.response.PetResponseDTO;
import com.fiap.clyvocaresc.entity.Owner;
import com.fiap.clyvocaresc.entity.Pet;
import com.fiap.clyvocaresc.entity.Species;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.OwnerRepository;
import com.fiap.clyvocaresc.repository.PetRepository;
import com.fiap.clyvocaresc.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Gerencia o cadastro do Pet, a entidade central de todo o sistema — é em cima dela
 * que o histórico longitudinal se constrói: toda Appointment, Exam, Treatment e
 * Vaccination aponta pra um Pet. A regra de negócio principal é de posse: um pet só
 * pode ser criado vinculado a um Owner e uma Species que realmente existam. O método
 * findByOwner é o ponto de entrada que o app do tutor usa pra listar seus animais.
 * <p>
 * Endpoints necessários: GET /api/pets/{id}, GET /api/owners/{ownerId}/pets,
 * POST /api/pets, PUT /api/pets/{id}, DELETE /api/pets/{id}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PetService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;
    private final SpeciesRepository speciesRepository;

    /** Lista todos os pets de um tutor específico, base da tela "meus pets" do app. */
    @Transactional(readOnly = true)
    public List<PetResponseDTO> findByOwner(Long ownerId) {
        return petRepository.findByOwnerId(ownerId).stream().map(this::toResponse).toList();
    }

    /** Busca um pet específico por id, base da "ficha do pet" com todo o histórico. */
    public PetResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Cria um novo pet, marcando a data de cadastro como hoje e validando Owner/Species existentes. */
    @Transactional(readOnly = true)
    public PetResponseDTO create(PetRequestDTO dto) {
        Pet pet = new Pet();
        pet.setRegisteredAt(LocalDate.now());
        apply(pet, dto);
        return toResponse(petRepository.save(pet));
    }

    /** Atualiza os dados de um pet existente. */
    public PetResponseDTO update(Long id, PetRequestDTO dto) {
        Pet pet = getOrThrow(id);
        apply(pet, dto);
        return toResponse(petRepository.save(pet));
    }

    /** Remove um pet do cadastro (uso raro; geralmente prefere-se manter o histórico). */
    public void delete(Long id) {
        petRepository.delete(getOrThrow(id));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    private Pet getOrThrow(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com id " + id));
    }

    /** Copia os campos do DTO pra entidade, resolvendo Owner e Species obrigatórios. */
    private void apply(Pet pet, PetRequestDTO dto) {
        pet.setName(dto.name());
        pet.setBirthDate(dto.birthDate());
        pet.setSex(dto.sex());
        pet.setCurrentWeight(dto.currentWeight() != null ? BigDecimal.valueOf(dto.currentWeight()) : null);
        pet.setMicrochip(dto.microchip());
        pet.setBreed(dto.breed());
        pet.setSize(dto.size());
        pet.setPhotoUrl(dto.photoUrl());

        Owner owner = ownerRepository.findById(dto.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com id " + dto.ownerId()));
        pet.setOwner(owner);

        Species species = speciesRepository.findById(dto.speciesId())
                .orElseThrow(() -> new ResourceNotFoundException("Espécie não encontrada com id " + dto.speciesId()));
        pet.setSpecies(species);
    }

    /** Converte a entidade em DTO de saída, incluindo nome do tutor e da espécie já resolvidos. */
    private PetResponseDTO toResponse(Pet pet) {
        return new PetResponseDTO(
                pet.getId(), pet.getName(), pet.getBirthDate(), pet.getSex(),
                pet.getCurrentWeight() != null ? pet.getCurrentWeight().doubleValue() : null,
                pet.getMicrochip(), pet.getBreed(), pet.getSize(), pet.getPhotoUrl(), pet.getRegisteredAt(),
                pet.getOwner().getId(), pet.getOwner().getName(), pet.getSpecies().getName()
        );
    }
}
