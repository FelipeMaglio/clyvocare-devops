package com.fiap.clyvocaresc.controller;


import com.fiap.clyvocaresc.dto.response.ReminderResponseDTO;
import com.fiap.clyvocaresc.service.OwnerService;
import com.fiap.clyvocaresc.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe a central de notificações do tutor. Não tem POST — Reminder só nasce como
 * efeito colateral de AppointmentService/VaccinationService. Resolve o Owner a
 * partir do token (via OwnerService) antes de consultar os lembretes, pra garantir
 * que o tutor só veja os próprios alertas, nunca de outro tutor.
 */
@RestController
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final OwnerService ownerService;

    /** Lista os lembretes pendentes do tutor autenticado. */
    @GetMapping("/api/owners/me/reminders")
    public List<ReminderResponseDTO> getMyPendingReminders(Authentication authentication) {
        Long ownerId = ownerService.findByUsername(authentication.getName()).id();
        return reminderService.findPendingByOwner(ownerId);
    }

    /** Marca um lembrete como enviado. */
    @PatchMapping("/api/reminders/{id}/sent")
    public ReminderResponseDTO markAsSent(@PathVariable Long id) {
        return reminderService.markAsSent(id);
    }

    /** Marca um lembrete como confirmado pelo tutor. */
    @PatchMapping("/api/reminders/{id}/confirmed")
    public ReminderResponseDTO markAsConfirmed(@PathVariable Long id) {
        return reminderService.markAsConfirmed(id);
    }
}
