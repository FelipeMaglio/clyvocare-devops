package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.request.AppointmentCompleteRequestDTO;
import com.fiap.clyvocaresc.dto.request.AppointmentCreateRequestDTO;
import com.fiap.clyvocaresc.dto.response.AppointmentResponseDTO;
import com.fiap.clyvocaresc.entity.*;
import com.fiap.clyvocaresc.entity.enums.AppointmentStatus;
import com.fiap.clyvocaresc.entity.enums.ReminderChannel;
import com.fiap.clyvocaresc.entity.enums.ReminderStatus;
import com.fiap.clyvocaresc.entity.enums.ReminderType;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gerencia o ciclo de vida de uma consulta veterinária — um dos dois fluxos completos
 * do sistema (requisito 4). Não tem um `update()` genérico: separa `schedule()`
 * (agendamento, status SCHEDULED) de `complete()` (o veterinário fecha a consulta com
 * diagnóstico, status COMPLETED). É em `complete()` que o pilar de alertas inteligentes
 * entra em ação — o sistema cria automaticamente um Reminder de retorno 15 dias depois,
 * sem que ninguém precise lembrar de criar o lembrete manualmente. Também atualiza o
 * peso atual do pet quando informado na conclusão, alimentando o histórico longitudinal.
 * <p>
 * Endpoints necessários: GET /api/pets/{petId}/appointments, GET /api/appointments/{id},
 * POST /api/appointments (agendar), PATCH /api/appointments/{id}/complete (concluir →
 * dispara Reminder), PATCH /api/appointments/{id}/cancel.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int FOLLOW_UP_DAYS = 15;

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final ClinicRepository clinicRepository;
    private final ReminderRepository reminderRepository;

    /** Lista o histórico de consultas de um pet, mais recente primeiro — base da "ficha clínica". */
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> findByPet(Long petId) {
        return appointmentRepository.findByPetIdOrderByAppointmentDateDesc(petId)
                .stream().map(this::toResponse).toList();
    }

    /** Busca uma consulta específica por id. */
    @Transactional(readOnly = true)
    public AppointmentResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Agenda uma nova consulta com status SCHEDULED, vinculando pet e opcionalmente veterinário/clínica. */
    @Transactional
    public AppointmentResponseDTO schedule(AppointmentCreateRequestDTO dto) {
        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com id " + dto.petId()));

        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(dto.appointmentDate());
        appointment.setType(dto.type());
        appointment.setReason(dto.reason());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPet(pet);

        if (dto.veterinarianId() != null) {
            appointment.setVeterinarian(veterinarianRepository.findById(dto.veterinarianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado com id " + dto.veterinarianId())));
        }
        if (dto.clinicId() != null) {
            appointment.setClinic(clinicRepository.findById(dto.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada com id " + dto.clinicId())));
        }

        return toResponse(appointmentRepository.save(appointment));
    }

    /**
     * Conclui a consulta: registra diagnóstico/observações/peso, marca status COMPLETED,
     * atualiza o peso atual do pet e dispara a criação automática do Reminder de retorno.
     * Bloqueia a conclusão de uma consulta que já esteja COMPLETED.
     */
    @Transactional
    public AppointmentResponseDTO complete(Long id, AppointmentCompleteRequestDTO dto) {
        Appointment appointment = getOrThrow(id);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Essa consulta já foi concluída");
        }

        appointment.setDiagnosis(dto.diagnosis());
        appointment.setNotes(dto.notes());
        if (dto.weightAtVisit() != null) {
            appointment.setWeightAtVisit(BigDecimal.valueOf(dto.weightAtVisit()));
            appointment.getPet().setCurrentWeight(BigDecimal.valueOf(dto.weightAtVisit()));
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);

        Appointment saved = appointmentRepository.save(appointment);

        createFollowUpReminder(saved);

        return toResponse(saved);
    }

    /** Cria o Reminder automático de retorno, 15 dias após a data da consulta concluída. */
    @Transactional
    private void createFollowUpReminder(Appointment appointment) {
        Reminder reminder = new Reminder();
        reminder.setType(ReminderType.APPOINTMENT);
        reminder.setEventDate(appointment.getAppointmentDate().plusDays(FOLLOW_UP_DAYS));
        reminder.setMessage("Retorno recomendado para " + appointment.getPet().getName() +
                " após consulta de " + appointment.getAppointmentDate());
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setChannel(ReminderChannel.PUSH);
        reminder.setPet(appointment.getPet());
        reminder.setOwner(appointment.getPet().getOwner());
        reminderRepository.save(reminder);
    }

    /** Cancela uma consulta agendada, marcando status CANCELED. */
    @Transactional
    public void cancel(Long id) {
        Appointment appointment = getOrThrow(id);
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    @Transactional
    private Appointment getOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com id " + id));
    }

    /** Converte a entidade em DTO de saída. */
    @Transactional
    private AppointmentResponseDTO toResponse(Appointment a) {
        return new AppointmentResponseDTO(
                a.getId(), a.getAppointmentDate(), a.getType(), a.getReason(), a.getDiagnosis(), a.getNotes(),
                a.getWeightAtVisit() != null ? a.getWeightAtVisit().doubleValue() : null, a.getStatus(),
                a.getPet().getId(), a.getPet().getName(),
                a.getVeterinarian() != null ? a.getVeterinarian().getName() : null,
                a.getClinic() != null ? a.getClinic().getName() : null
        );
    }
}
