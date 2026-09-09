package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.request.ClinicRequestDTO;
import com.fiap.clyvocaresc.dto.response.ClinicResponseDTO;
import com.fiap.clyvocaresc.entity.City;
import com.fiap.clyvocaresc.entity.Clinic;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.CityRepository;
import com.fiap.clyvocaresc.repository.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Gerencia o cadastro de clínicas veterinárias. Não tem cálculo de negócio complexo,
 * mas é peça-chave de rastreabilidade: toda Appointment/Vaccination pode referenciar
 * a clínica onde ocorreu, e o vínculo com City sustenta o futuro módulo de alertas
 * regionais. A criação define automaticamente a data de assinatura como "hoje".
 * <p>
 * Endpoints necessários: GET /api/clinics, GET /api/clinics/{id}, POST /api/clinics,
 * PUT /api/clinics/{id}, DELETE /api/clinics/{id} — cadastro restrito a CLINIC_ADMIN.
 */
@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final CityRepository cityRepository;

    /** Lista todas as clínicas cadastradas. */
    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> findAll() {
        return clinicRepository.findAll().stream().map(this::toResponse).toList();
    }

    /** Busca uma clínica por id; lança 404 se não existir. */
    @Transactional(readOnly = true)
    public ClinicResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Cria uma nova clínica, marcando a data de assinatura como a data atual. */
    @Transactional
    public ClinicResponseDTO create(ClinicRequestDTO dto) {
        Clinic clinic = new Clinic();
        clinic.setSubscriptionDate(LocalDate.now());
        apply(clinic, dto);
        return toResponse(clinicRepository.save(clinic));
    }

    /** Atualiza os dados de uma clínica existente. */
    @Transactional
    public ClinicResponseDTO update(Long id, ClinicRequestDTO dto) {
        Clinic clinic = getOrThrow(id);
        apply(clinic, dto);
        return toResponse(clinicRepository.save(clinic));
    }

    /** Remove uma clínica do cadastro. */
    @Transactional
    public void delete(Long id) {
        clinicRepository.delete(getOrThrow(id));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    @Transactional
    private Clinic getOrThrow(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada com id " + id));
    }

    /** Copia os campos do DTO pra entidade, resolvendo a City relacionada quando informada. */
    @Transactional
    private void apply(Clinic clinic, ClinicRequestDTO dto) {
        clinic.setName(dto.name());
        clinic.setTaxId(dto.taxId());
        clinic.setPhone(dto.phone());
        clinic.setEmail(dto.email());
        clinic.setAddress(dto.address());
        clinic.setSubscriptionPlan(dto.subscriptionPlan());

        if (dto.cityId() != null) {
            City city = cityRepository.findById(dto.cityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cidade não encontrada com id " + dto.cityId()));
            clinic.setCity(city);
        }
    }

    /** Converte a entidade em DTO de saída. */
    @Transactional
    private ClinicResponseDTO toResponse(Clinic clinic) {
        return new ClinicResponseDTO(
                clinic.getId(), clinic.getName(), clinic.getTaxId(), clinic.getPhone(), clinic.getEmail(),
                clinic.getAddress(), clinic.getSubscriptionPlan(), clinic.getSubscriptionDate(),
                clinic.getCity() != null ? clinic.getCity().getName() : null
        );
    }
}
