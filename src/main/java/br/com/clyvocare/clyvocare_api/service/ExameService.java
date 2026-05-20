package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.ExameRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.ExameResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Consulta;
import br.com.clyvocare.clyvocare_api.entity.Exame;
import br.com.clyvocare.clyvocare_api.entity.Pet;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.ConsultaRepository;
import br.com.clyvocare.clyvocare_api.repository.ExameRepository;
import br.com.clyvocare.clyvocare_api.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExameService {

    private final ExameRepository exameRepository;
    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;

    @Transactional
    @CacheEvict(value = "exames", allEntries = true)
    public ExameResponseDTO criar(ExameRequestDTO dto) {
        // Regra: data resultado não pode ser anterior à solicitação
        if (dto.getDataResultado() != null
                && dto.getDataResultado().isBefore(dto.getDataSolicitacao())) {
            throw new RegraNegocioException("A data do resultado não pode ser anterior à data de solicitação.");
        }
        Exame exame = new Exame();
        preencherExame(exame, dto);
        return toResponseDTO(exameRepository.save(exame));
    }
    @Transactional(readOnly = true)
    @Cacheable("exames")
    public Page<ExameResponseDTO> listar(Pageable pageable) {
        return exameRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public ExameResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<ExameResponseDTO> buscarPorPet(Long idPet, Pageable pageable) {
        petRepository.findById(idPet)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + idPet));
        return exameRepository.findByPetIdPetOrderByDataSolicitacaoDesc(idPet, pageable)
                .map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ExameResponseDTO> buscarPorTipo(String tipo, Pageable pageable) {
        return exameRepository.findByTipoExameContainingIgnoreCase(tipo, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ExameResponseDTO> buscarPorConsulta(Long idConsulta, Pageable pageable) {
        return exameRepository.findByConsultaIdConsulta(idConsulta, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ExameResponseDTO> buscarSemResultado(Pageable pageable) {
        return exameRepository.findByDataResultadoIsNull(pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "exames", allEntries = true)
    public ExameResponseDTO atualizar(Long id, ExameRequestDTO dto) {
        Exame exame = buscarEntidade(id);
        if (dto.getDataResultado() != null
                && dto.getDataResultado().isBefore(dto.getDataSolicitacao())) {
            throw new RegraNegocioException("A data do resultado não pode ser anterior à data de solicitação.");
        }
        preencherExame(exame, dto);
        return toResponseDTO(exameRepository.save(exame));
    }

    @Transactional
    @CacheEvict(value = "exames", allEntries = true)
    public void deletar(Long id) {
        exameRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Exame buscarEntidade(Long id) {
        return exameRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Exame não encontrado com ID: " + id));
    }

    private void preencherExame(Exame exame, ExameRequestDTO dto) {
        Pet pet = petRepository.findById(dto.getIdPet())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + dto.getIdPet()));
        exame.setPet(pet);
        exame.setTipoExame(dto.getTipoExame());
        exame.setDataSolicitacao(dto.getDataSolicitacao());
        exame.setDataResultado(dto.getDataResultado());
        exame.setResultado(dto.getResultado());
        exame.setArquivoUrl(dto.getArquivoUrl());
        exame.setLaboratorio(dto.getLaboratorio());

        if (dto.getIdConsulta() != null) {
            Consulta consulta = consultaRepository.findById(dto.getIdConsulta())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Consulta não encontrada com ID: " + dto.getIdConsulta()));
            exame.setConsulta(consulta);
        }
    }

    private ExameResponseDTO toResponseDTO(Exame e) {
        return ExameResponseDTO.builder()
                .idExame(e.getIdExame())
                .tipoExame(e.getTipoExame())
                .dataSolicitacao(e.getDataSolicitacao())
                .dataResultado(e.getDataResultado())
                .resultadoDisponivel(e.getDataResultado() != null)
                .resultado(e.getResultado())
                .arquivoUrl(e.getArquivoUrl())
                .laboratorio(e.getLaboratorio())
                .idPet(e.getPet().getIdPet())
                .nomePet(e.getPet().getNome())
                .idConsulta(e.getConsulta() != null ? e.getConsulta().getIdConsulta() : null)
                .build();
    }
}
