package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.response.ReminderResponseDTO;
import com.fiap.clyvocaresc.entity.Reminder;
import com.fiap.clyvocaresc.entity.enums.ReminderStatus;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Expõe os alertas gerados automaticamente por AppointmentService e VaccinationService —
 * é a "central de notificações" do pilar de alertas inteligentes. Não tem `create()`
 * público de propósito: um Reminder só nasce como efeito colateral de outro fluxo de
 * negócio (consulta concluída ou vacina aplicada), nunca por criação manual direta via API.
 * O papel deste service é permitir consulta dos pendentes e a atualização de status
 * conforme o ciclo de vida da notificação avança (enviada, confirmada pelo tutor).
 * <p>
 * Endpoints necessários: GET /api/owners/me/reminders (alertas pendentes do tutor logado),
 * PATCH /api/reminders/{id}/sent, PATCH /api/reminders/{id}/confirmed.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;

    /** Lista os lembretes pendentes de um tutor, base da tela de notificações do app. */
    @Transactional(readOnly = true)
    public List<ReminderResponseDTO> findPendingByOwner(Long ownerId) {
        return reminderRepository.findByOwnerIdAndStatus(ownerId, ReminderStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    /** Marca um lembrete como enviado (SENT), tipicamente após o push notification disparar. */
    public ReminderResponseDTO markAsSent(Long id) {
        Reminder reminder = getOrThrow(id);
        reminder.setStatus(ReminderStatus.SENT);
        return toResponse(reminderRepository.save(reminder));
    }

    /** Marca um lembrete como confirmado (CONFIRMED) pelo tutor, ex: "já levei pra vacinar". */
    public ReminderResponseDTO markAsConfirmed(Long id) {
        Reminder reminder = getOrThrow(id);
        reminder.setStatus(ReminderStatus.CONFIRMED);
        return toResponse(reminderRepository.save(reminder));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    private Reminder getOrThrow(Long id) {
        return reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lembrete não encontrado com id " + id));
    }

    /** Converte a entidade em DTO de saída. */
    private ReminderResponseDTO toResponse(Reminder r) {
        return new ReminderResponseDTO(
                r.getId(), r.getType(), r.getEventDate(), r.getMessage(), r.getStatus(), r.getChannel(),
                r.getPet().getId(), r.getPet().getName()
        );
    }
}
