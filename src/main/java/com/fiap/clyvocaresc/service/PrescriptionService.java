package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.request.PrescriptionRequestDTO;
import com.fiap.clyvocaresc.dto.response.PrescriptionResponseDTO;
import com.fiap.clyvocaresc.entity.CatalogItem;
import com.fiap.clyvocaresc.entity.Prescription;
import com.fiap.clyvocaresc.entity.Treatment;
import com.fiap.clyvocaresc.entity.enums.CatalogItemType;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.CatalogItemRepository;
import com.fiap.clyvocaresc.repository.PrescriptionRepository;
import com.fiap.clyvocaresc.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Detalha a dosagem de um medicamento dentro de um Treatment (posologia, frequência,
 * duração). Espelha a regra do VaccinationService: valida que o CatalogItem escolhido
 * é do tipo MEDICATION, não VACCINE, mantendo a integridade semântica do catálogo
 * unificado — evita registrar "vacina" como remédio prescrito por engano.
 * <p>
 * Endpoints necessários: GET /api/treatments/{treatmentId}/prescriptions,
 * POST /api/prescriptions.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final TreatmentRepository treatmentRepository;
    private final CatalogItemRepository catalogItemRepository;

    /** Lista as prescrições associadas a um tratamento específico. */
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> findByTreatment(Long treatmentId) {
        return prescriptionRepository.findByTreatmentId(treatmentId).stream().map(this::toResponse).toList();
    }

    /** Registra uma prescrição, validando que o item de catálogo escolhido é um medicamento. */
    public PrescriptionResponseDTO create(PrescriptionRequestDTO dto) {
        Treatment treatment = treatmentRepository.findById(dto.treatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tratamento não encontrado com id " + dto.treatmentId()));

        CatalogItem catalogItem = catalogItemRepository.findById(dto.catalogItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item de catálogo não encontrado com id " + dto.catalogItemId()));

        if (catalogItem.getType() != CatalogItemType.MEDICATION) {
            throw new IllegalArgumentException("O item de catálogo informado não é um medicamento (type = " + catalogItem.getType() + ")");
        }

        Prescription prescription = new Prescription();
        prescription.setDosage(dto.dosage());
        prescription.setFrequency(dto.frequency());
        prescription.setDurationDays(dto.durationDays());
        prescription.setInstructions(dto.instructions());
        prescription.setTreatment(treatment);
        prescription.setCatalogItem(catalogItem);

        return toResponse(prescriptionRepository.save(prescription));
    }

    /** Converte a entidade em DTO de saída. */
    private PrescriptionResponseDTO toResponse(Prescription p) {
        return new PrescriptionResponseDTO(
                p.getId(), p.getDosage(), p.getFrequency(), p.getDurationDays(), p.getInstructions(),
                p.getTreatment().getId(), p.getCatalogItem().getName()
        );
    }
}
