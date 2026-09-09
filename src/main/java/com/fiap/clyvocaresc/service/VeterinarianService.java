package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.response.VeterinarianResponseDTO;
import com.fiap.clyvocaresc.dto.request.VeterinarianUpdateRequestDTO;
import com.fiap.clyvocaresc.entity.Clinic;
import com.fiap.clyvocaresc.entity.Veterinarian;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.ClinicRepository;
import com.fiap.clyvocaresc.repository.VeterinarianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gerencia o perfil do veterinário após o cadastro inicial. Assim como Owner, não tem
 * `create()` — o Veterinarian nasce dentro de AuthService.registerVeterinarian() junto
 * com o User. Aqui o veterinário edita especialidade/contato e pode trocar de clínica,
 * e o sistema pode listar veterinários de uma clínica pra o tutor escolher com quem agendar.
 * <p>
 * Endpoints necessários: GET /api/veterinarians, GET /api/veterinarians/{id},
 * GET /api/veterinarians/me, PUT /api/veterinarians/me.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VeterinarianService {

    private final VeterinarianRepository veterinarianRepository;
    private final ClinicRepository clinicRepository;

    /** Lista todos os veterinários cadastrados. */
    @Transactional(readOnly = true)
    public List<VeterinarianResponseDTO> findAll() {
        return veterinarianRepository.findAll().stream().map(this::toResponse).toList();
    }

    /** Busca um veterinário por id (uso interno/administrativo). */
    @Transactional(readOnly = true)
    public VeterinarianResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Busca o perfil do veterinário a partir do username autenticado, usado no endpoint "/veterinarians/me". */
    @Transactional(readOnly = true)
    public VeterinarianResponseDTO findByUsername(String username) {
        Veterinarian vet = veterinarianRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado para o usuário " + username));
        return toResponse(vet);
    }

    /** Atualiza especialidade/contato/clínica do próprio veterinário logado, identificado pelo username do token. */
    public VeterinarianResponseDTO updateOwnProfile(String username, VeterinarianUpdateRequestDTO dto) {
        Veterinarian vet = veterinarianRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado para o usuário " + username));

        vet.setName(dto.name());
        vet.setSpecialty(dto.specialty());
        vet.setEmail(dto.email());
        vet.setPhone(dto.phone());

        if (dto.clinicId() != null) {
            Clinic clinic = clinicRepository.findById(dto.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada com id " + dto.clinicId()));
            vet.setClinic(clinic);
        }

        return toResponse(veterinarianRepository.save(vet));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    private Veterinarian getOrThrow(Long id) {
        return veterinarianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado com id " + id));
    }

    /** Converte a entidade em DTO de saída. */
    private VeterinarianResponseDTO toResponse(Veterinarian vet) {
        return new VeterinarianResponseDTO(
                vet.getId(), vet.getName(), vet.getLicenseNumber(), vet.getSpecialty(),
                vet.getEmail(), vet.getPhone(),
                vet.getClinic() != null ? vet.getClinic().getName() : null
        );
    }
}
