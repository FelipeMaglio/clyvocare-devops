package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.AppointmentCompleteRequestDTO;
import com.fiap.clyvocaresc.dto.request.AppointmentCreateRequestDTO;
import com.fiap.clyvocaresc.dto.response.AppointmentResponseDTO;
import com.fiap.clyvocaresc.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller do fluxo de consulta veterinária, o primeiro dos dois fluxos completos
 * do sistema. Note os dois PATCH separados (`/complete` e `/cancel`) em vez de um
 * PUT genérico — cada um dispara uma regra de negócio diferente no Service (a
 * conclusão gera automaticamente um Reminder de retorno).
 */
@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /** Lista o histórico de consultas de um pet. */
    @GetMapping("/api/pets/{petId}/appointments")
    public List<AppointmentResponseDTO> findByPet(@PathVariable Long petId) {
        return appointmentService.findByPet(petId);
    }

    /** Busca uma consulta específica por id. */
    @GetMapping("/api/appointments/{id}")
    public AppointmentResponseDTO findById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    /** Agenda uma nova consulta (status inicial SCHEDULED). */
    @PostMapping("/api/appointments")
    public ResponseEntity<AppointmentResponseDTO> schedule(@Valid @RequestBody AppointmentCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(appointmentService.schedule(dto));
    }

    /** Conclui a consulta com diagnóstico — dispara a criação automática do Reminder de retorno. */
    @PatchMapping("/api/appointments/{id}/complete")
    public AppointmentResponseDTO complete(@PathVariable Long id, @Valid @RequestBody AppointmentCompleteRequestDTO dto) {
        return appointmentService.complete(id, dto);
    }

    /** Cancela uma consulta agendada. */
    @PatchMapping("/api/appointments/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
