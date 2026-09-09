package com.fiap.clyvocaresc.service;


import com.fiap.clyvocaresc.dto.request.VaccinationRequestDTO;
import com.fiap.clyvocaresc.dto.response.VaccinationResponseDTO;
import com.fiap.clyvocaresc.entity.*;
import com.fiap.clyvocaresc.entity.enums.CatalogItemType;
import com.fiap.clyvocaresc.entity.enums.ReminderChannel;
import com.fiap.clyvocaresc.entity.enums.ReminderStatus;
import com.fiap.clyvocaresc.entity.enums.ReminderType;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Registra a aplicação de uma vacina em um pet — o segundo fluxo completo do sistema
 * (requisito 4) e o coração do pilar de monitoramento preventivo. A regra de negócio
 * central é o cálculo automático de nextDoseDate = applicationDate + boosterIntervalDays
 * (lido do CatalogItem), e a validação de que o item escolhido é mesmo do tipo VACCINE
 * (não deixa aplicar um medicamento como se fosse vacina). Ao final, gera automaticamente
 * um Reminder do tipo VACCINE pra data do próximo reforço, ligando prevenção a alerta
 * inteligente sem intervenção manual do usuário.
 * <p>
 * Endpoints necessários: GET /api/pets/{petId}/vaccinations,
 * POST /api/vaccinations (aplicar vacina → calcula nextDoseDate e dispara Reminder).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final PetRepository petRepository;
    private final CatalogItemRepository catalogItemRepository;
    private final AppointmentRepository appointmentRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final ReminderRepository reminderRepository;

    /** Lista o histórico de vacinação de um pet, mais recente primeiro. */
    @Transactional(readOnly = true)
    public List<VaccinationResponseDTO> findByPet(Long petId) {
        return vaccinationRepository.findByPetIdOrderByApplicationDateDesc(petId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Registra a aplicação de uma vacina: valida que o CatalogItem é do tipo VACCINE,
     * calcula a próxima dose (se o item tiver intervalo de reforço definido) e cria
     * o Reminder correspondente automaticamente.
     */
    @Transactional
    public VaccinationResponseDTO apply(VaccinationRequestDTO dto) {
        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com id " + dto.petId()));

        CatalogItem catalogItem = catalogItemRepository.findById(dto.catalogItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item de catálogo não encontrado com id " + dto.catalogItemId()));

        if (catalogItem.getType() != CatalogItemType.VACCINE) {
            throw new IllegalArgumentException("O item de catálogo informado não é uma vacina (type = " + catalogItem.getType() + ")");
        }

        Vaccination vaccination = new Vaccination();
        vaccination.setApplicationDate(dto.applicationDate());
        vaccination.setBatch(dto.batch());
        vaccination.setExpirationDate(dto.expirationDate());
        vaccination.setPet(pet);
        vaccination.setCatalogItem(catalogItem);

        if (dto.appointmentId() != null) {
            vaccination.setAppointment(appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com id " + dto.appointmentId())));
        }
        if (dto.veterinarianId() != null) {
            vaccination.setVeterinarian(veterinarianRepository.findById(dto.veterinarianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado com id " + dto.veterinarianId())));
        }

        LocalDate nextDoseDate = null;
        if (catalogItem.getBoosterIntervalDays() != null) {
            nextDoseDate = dto.applicationDate().plusDays(catalogItem.getBoosterIntervalDays());
            vaccination.setNextDoseDate(nextDoseDate);
        }

        Vaccination saved = vaccinationRepository.save(vaccination);

        if (nextDoseDate != null) {
            createBoosterReminder(saved, nextDoseDate);
        }

        return toResponse(saved);
    }

    /** Cria o Reminder automático de reforço de vacina, na data calculada de próxima dose. */
    private void createBoosterReminder(Vaccination vaccination, LocalDate nextDoseDate) {
        Reminder reminder = new Reminder();
        reminder.setType(ReminderType.VACCINE);
        reminder.setEventDate(nextDoseDate);
        reminder.setMessage("Reforço de " + vaccination.getCatalogItem().getName() +
                " para " + vaccination.getPet().getName() + " previsto para " + nextDoseDate);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setChannel(ReminderChannel.PUSH);
        reminder.setPet(vaccination.getPet());
        reminder.setOwner(vaccination.getPet().getOwner());
        reminderRepository.save(reminder);
    }

    /** Converte a entidade em DTO de saída. */
    private VaccinationResponseDTO toResponse(Vaccination v) {
        return new VaccinationResponseDTO(
                v.getId(), v.getApplicationDate(), v.getBatch(), v.getNextDoseDate(), v.getExpirationDate(),
                v.getPet().getId(), v.getPet().getName(), v.getCatalogItem().getName(),
                v.getVeterinarian() != null ? v.getVeterinarian().getName() : null
        );
    }
}
