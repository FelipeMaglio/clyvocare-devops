package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.request.TreatmentRequestDTO;
import com.fiap.clyvocaresc.dto.response.TreatmentResponseDTO;
import com.fiap.clyvocaresc.entity.Appointment;
import com.fiap.clyvocaresc.entity.Pet;
import com.fiap.clyvocaresc.entity.Treatment;
import com.fiap.clyvocaresc.entity.enums.TreatmentStatus;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.AppointmentRepository;
import com.fiap.clyvocaresc.repository.PetRepository;
import com.fiap.clyvocaresc.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Gerencia planos terapêuticos em andamento (ex: "antibiótico por 10 dias"), parte
 * do acompanhamento clínico contínuo. A regra de negócio é o controle de status:
 * todo tratamento nasce ACTIVE, e a transição pra COMPLETED/SUSPENDED é feita por
 * métodos dedicados (complete/suspend), não por um update genérico — isso impede
 * que o cliente da API mande qualquer status arbitrário sem passar pela regra certa.
 * <p>
 * Endpoints necessários: GET /api/pets/{petId}/treatments, POST /api/treatments,
 * PUT /api/treatments/{id}, PATCH /api/treatments/{id}/complete,
 * PATCH /api/treatments/{id}/suspend.
 */
@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;

    /** Lista o histórico de tratamentos de um pet. */
    @Transactional(readOnly = true)
    public List<TreatmentResponseDTO> findByPet(Long petId) {
        return treatmentRepository.findByPetId(petId).stream().map(this::toResponse).toList();
    }

    /** Inicia um novo tratamento com status ACTIVE. */
    @Transactional
    public TreatmentResponseDTO create(TreatmentRequestDTO dto) {
        Treatment treatment = new Treatment();
        treatment.setStatus(TreatmentStatus.ACTIVE);
        apply(treatment, dto);
        return toResponse(treatmentRepository.save(treatment));
    }

    /** Atualiza descrição/datas de um tratamento existente, sem alterar o status. */
    @Transactional
    public TreatmentResponseDTO update(Long id, TreatmentRequestDTO dto) {
        Treatment treatment = getOrThrow(id);
        apply(treatment, dto);
        return toResponse(treatmentRepository.save(treatment));
    }

    /** Marca o tratamento como COMPLETED e fixa a data de término como hoje. */
    @Transactional
    public TreatmentResponseDTO complete(Long id) {
        Treatment treatment = getOrThrow(id);
        treatment.setStatus(TreatmentStatus.COMPLETED);
        treatment.setEndDate(LocalDate.now());
        return toResponse(treatmentRepository.save(treatment));
    }

    /** Marca o tratamento como SUSPENDED (ex: reação adversa, interrupção pelo tutor). */
    @Transactional
    public TreatmentResponseDTO suspend(Long id) {
        Treatment treatment = getOrThrow(id);
        treatment.setStatus(TreatmentStatus.SUSPENDED);
        return toResponse(treatmentRepository.save(treatment));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    @Transactional
    private Treatment getOrThrow(Long id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tratamento não encontrado com id " + id));
    }

    /** Copia os campos do DTO pra entidade, resolvendo Pet obrigatório e Appointment opcional. */
    @Transactional
    private void apply(Treatment treatment, TreatmentRequestDTO dto) {
        treatment.setDescription(dto.description());
        treatment.setStartDate(dto.startDate());
        treatment.setEndDate(dto.endDate());

        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com id " + dto.petId()));
        treatment.setPet(pet);

        if (dto.appointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com id " + dto.appointmentId()));
            treatment.setAppointment(appointment);
        }
    }

    /** Converte a entidade em DTO de saída. */
    @Transactional
    private TreatmentResponseDTO toResponse(Treatment t) {
        return new TreatmentResponseDTO(
                t.getId(), t.getDescription(), t.getStartDate(), t.getEndDate(), t.getStatus(),
                t.getPet().getId(), t.getPet().getName()
        );
    }
}
