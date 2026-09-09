package com.fiap.clyvocaresc.service;


import com.fiap.clyvocaresc.dto.response.OwnerResponseDTO;
import com.fiap.clyvocaresc.dto.request.OwnerUpdateRequestDTO;
import com.fiap.clyvocaresc.entity.City;
import com.fiap.clyvocaresc.entity.Owner;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.CityRepository;
import com.fiap.clyvocaresc.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gerencia o perfil do tutor (Owner) após o cadastro inicial. Não possui método `create()`
 * de propósito: o Owner nasce dentro de AuthService.registerOwner(), junto com o User,
 * numa única transação (regra do FK obrigatório entre as duas tabelas). Esse service cuida
 * apenas do pós-cadastro — o tutor consultando/editando o próprio perfil, e outras camadas
 * do sistema (ex: veterinário atendendo um pet) consultando quem é o dono. A checagem de
 * "só o próprio tutor edita seu perfil" é feita aqui comparando o username autenticado com
 * o dono do registro, reforçando o que a Security já filtra por token.
 * <p>
 * Endpoints necessários: GET /api/owners/me, PUT /api/owners/me,
 * GET /api/owners/{id} (uso interno, restrito a VETERINARIAN/CLINIC_ADMIN).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final CityRepository cityRepository;

    /**
     * Busca o perfil de um tutor por id (uso interno/administrativo, não é o próprio tutor logado).
     */
    @Transactional(readOnly = true)
    public OwnerResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /**
     * Busca o perfil do tutor a partir do username autenticado, usado no endpoint "/owners/me".
     */
    @Transactional(readOnly = true)
    public OwnerResponseDTO findByUsername(String username) {
        Owner owner = ownerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado para o usuário " + username));
        return toResponse(owner);
    }

    /**
     * Atualiza nome/telefone/cidade do próprio tutor logado, identificado pelo username do token JWT.
     */
    public OwnerResponseDTO updateOwnProfile(String username, OwnerUpdateRequestDTO dto) {
        Owner owner = ownerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado para o usuário " + username));

        owner.setName(dto.name());
        owner.setPhone(dto.phone());

        if (dto.cityId() != null) {
            City city = cityRepository.findById(dto.cityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cidade não encontrada com id " + dto.cityId()));
            owner.setCity(city);
        }

        return toResponse(ownerRepository.save(owner));
    }

    /**
     * Busca interna com tratamento de "não encontrado" centralizado.
     */
    private Owner getOrThrow(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com id " + id));
    }

    /**
     * Converte a entidade em DTO de saída, incluindo contagem de pets e nome da cidade já resolvidos.
     */
    private OwnerResponseDTO toResponse(Owner owner) {
        return new OwnerResponseDTO(
                owner.getId(), owner.getName(), owner.getPhone(), owner.getDocument(),
                owner.getRegisteredAt(),
                owner.getCity() != null ? owner.getCity().getName() : null,
                owner.getPets() != null ? owner.getPets().size() : 0
        );
    }
}
