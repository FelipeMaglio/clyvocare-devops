package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.request.ExamRequestDTO;
import com.fiap.clyvocaresc.dto.response.ExamResponseDTO;
import com.fiap.clyvocaresc.entity.Appointment;
import com.fiap.clyvocaresc.entity.Exam;
import com.fiap.clyvocaresc.entity.Pet;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.AppointmentRepository;
import com.fiap.clyvocaresc.repository.ExamRepository;
import com.fiap.clyvocaresc.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Registra exames e resultados de diagnóstico, complementando o histórico longitudinal
 * do pet. Não tem lógica de negócio especial além de vincular corretamente ao Pet e,
 * opcionalmente, à Appointment de origem — a inteligência do sistema está concentrada
 * em Appointment/Vaccination, este service é propositalmente simples (CRUD com FK).
 * <p>
 * Endpoints necessários: GET /api/pets/{petId}/exams, GET /api/exams/{id},
 * POST /api/exams, PUT /api/exams/{id}, DELETE /api/exams/{id}.
 */
@Service
@RequiredArgsConstructor

public class ExamService {

    private final ExamRepository examRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;

    /** Lista o histórico de exames de um pet. */
    @Transactional(readOnly = true)
    public List<ExamResponseDTO> findByPet(Long petId) {
        return examRepository.findByPetId(petId).stream().map(this::toResponse).toList();
    }

    /** Busca um exame específico por id. */
    @Transactional(readOnly = true)
    public ExamResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Registra um novo exame (solicitação e, opcionalmente, resultado já preenchido). */
    @Transactional
    public ExamResponseDTO create(ExamRequestDTO dto) {
        Exam exam = new Exam();
        apply(exam, dto);
        return toResponse(examRepository.save(exam));
    }

    /** Atualiza um exame existente, tipicamente pra preencher o resultado depois da solicitação. */
    @Transactional
    public ExamResponseDTO update(Long id, ExamRequestDTO dto) {
        Exam exam = getOrThrow(id);
        apply(exam, dto);
        return toResponse(examRepository.save(exam));
    }

    /** Remove um exame do histórico. */
    @Transactional
    public void delete(Long id) {
        examRepository.delete(getOrThrow(id));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado. */
    private Exam getOrThrow(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado com id " + id));
    }

    /** Copia os campos do DTO pra entidade, resolvendo Pet obrigatório e Appointment opcional. */
    @Transactional
    private void apply(Exam exam, ExamRequestDTO dto) {
        exam.setExamType(dto.examType());
        exam.setRequestDate(dto.requestDate());
        exam.setResultDate(dto.resultDate());
        exam.setResult(dto.result());
        exam.setFileUrl(dto.fileUrl());
        exam.setLaboratory(dto.laboratory());

        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com id " + dto.petId()));
        exam.setPet(pet);

        if (dto.appointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com id " + dto.appointmentId()));
            exam.setAppointment(appointment);
        }
    }

    /** Converte a entidade em DTO de saída. */
    @Transactional
    private ExamResponseDTO toResponse(Exam exam) {
        return new ExamResponseDTO(
                exam.getId(), exam.getExamType(), exam.getRequestDate(), exam.getResultDate(),
                exam.getResult(), exam.getFileUrl(), exam.getLaboratory(),
                exam.getPet().getId(), exam.getPet().getName()
        );
    }
}
