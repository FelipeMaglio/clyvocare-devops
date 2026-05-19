package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.ConsultaRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.ConsultaResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.*;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;

    @Transactional
    @CacheEvict(value = "consultas", allEntries = true)
    public ConsultaResponseDTO criar(ConsultaRequestDTO dto) {
        Consulta consulta = new Consulta();
        preencherConsulta(consulta, dto);
        return toResponseDTO(consultaRepository.save(consulta));
    }
    @Transactional(readOnly = true)
    @Cacheable("consultas")
    public Page<ConsultaResponseDTO> listar(Pageable pageable) {
        return consultaRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public ConsultaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<ConsultaResponseDTO> buscarPorPet(Long idPet, Pageable pageable) {
        petRepository.findById(idPet)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + idPet));
        return consultaRepository
                .findByPetIdPetOrderByDataConsultaDesc(idPet, pageable)
                .map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ConsultaResponseDTO> buscarPorStatus(String status, Pageable pageable) {
        return consultaRepository.findByStatus(status.toUpperCase(), pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ConsultaResponseDTO> buscarPorClinica(Long idClinica, Pageable pageable) {
        return consultaRepository.findByClinicaIdClinica(idClinica, pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "consultas", allEntries = true)
    public ConsultaResponseDTO atualizar(Long id, ConsultaRequestDTO dto) {
        Consulta consulta = buscarEntidade(id);
        // Regra: consulta já realizada não pode ser alterada para AGENDADA
        if ("REALIZADA".equals(consulta.getStatus()) && "AGENDADA".equals(dto.getStatus())) {
            throw new RegraNegocioException("Uma consulta já realizada não pode voltar para AGENDADA.");
        }
        preencherConsulta(consulta, dto);
        return toResponseDTO(consultaRepository.save(consulta));
    }

    @Transactional
    @CacheEvict(value = "consultas", allEntries = true)
    public void deletar(Long id) {
        Consulta consulta = buscarEntidade(id);
        // Regra: só permite deletar consultas AGENDADAS ou CANCELADAS
        if ("REALIZADA".equals(consulta.getStatus())) {
            throw new RegraNegocioException("Não é possível excluir uma consulta já realizada.");
        }
        consultaRepository.delete(consulta);
    }
    @Transactional(readOnly = true)
    public Consulta buscarEntidade(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Consulta não encontrada com ID: " + id));
    }

    private void preencherConsulta(Consulta consulta, ConsultaRequestDTO dto) {
        Pet pet = petRepository.findById(dto.getIdPet())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + dto.getIdPet()));

        consulta.setPet(pet);
        consulta.setDataConsulta(dto.getDataConsulta());
        consulta.setTipo(dto.getTipo());
        consulta.setMotivo(dto.getMotivo());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setObservacoes(dto.getObservacoes());
        consulta.setPesoNaConsulta(dto.getPesoNaConsulta());
        consulta.setStatus(dto.getStatus());

        if (dto.getIdVeterinario() != null) {
            Veterinario vet = veterinarioRepository.findById(dto.getIdVeterinario())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Veterinário não encontrado com ID: " + dto.getIdVeterinario()));
            consulta.setVeterinario(vet);
        }

        if (dto.getIdClinica() != null) {
            Clinica clinica = clinicaRepository.findById(dto.getIdClinica())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Clínica não encontrada com ID: " + dto.getIdClinica()));
            consulta.setClinica(clinica);
        }
    }

    private ConsultaResponseDTO toResponseDTO(Consulta c) {
        return ConsultaResponseDTO.builder()
                .idConsulta(c.getIdConsulta())
                .dataConsulta(c.getDataConsulta())
                .tipo(c.getTipo())
                .motivo(c.getMotivo())
                .diagnostico(c.getDiagnostico())
                .observacoes(c.getObservacoes())
                .pesoNaConsulta(c.getPesoNaConsulta())
                .status(c.getStatus())
                .idPet(c.getPet().getIdPet())
                .nomePet(c.getPet().getNome())
                .nomeVeterinario(c.getVeterinario() != null ? c.getVeterinario().getNome() : null)
                .nomeClinica(c.getClinica() != null ? c.getClinica().getNome() : null)
                .build();
    }
}
