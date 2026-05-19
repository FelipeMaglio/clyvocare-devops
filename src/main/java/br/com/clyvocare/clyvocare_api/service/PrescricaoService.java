package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.PrescricaoRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.PrescricaoResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Medicamento;
import br.com.clyvocare.clyvocare_api.entity.Prescricao;
import br.com.clyvocare.clyvocare_api.entity.Tratamento;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.MedicamentoRepository;
import br.com.clyvocare.clyvocare_api.repository.PrescricaoRepository;
import br.com.clyvocare.clyvocare_api.repository.TratamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrescricaoService {

    private final PrescricaoRepository prescricaoRepository;
    private final TratamentoRepository tratamentoRepository;
    private final MedicamentoRepository medicamentoRepository;

    @Transactional
    @CacheEvict(value = "prescricoes", allEntries = true)
    public PrescricaoResponseDTO criar(PrescricaoRequestDTO dto) {
        Tratamento tratamento = tratamentoRepository.findById(dto.getIdTratamento())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tratamento não encontrado com ID: " + dto.getIdTratamento()));

        // Regra: só permite prescrever para tratamentos ATIVOS
        if (!"ATIVO".equals(tratamento.getStatus())) {
            throw new RegraNegocioException("Só é possível prescrever medicamentos para tratamentos ATIVOS.");
        }

        Medicamento medicamento = medicamentoRepository.findById(dto.getIdMedicamento())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Medicamento não encontrado com ID: " + dto.getIdMedicamento()));

        Prescricao prescricao = new Prescricao();
        prescricao.setTratamento(tratamento);
        prescricao.setMedicamento(medicamento);
        prescricao.setDosagem(dto.getDosagem());
        prescricao.setFrequencia(dto.getFrequencia());
        prescricao.setDuracaoDias(dto.getDuracaoDias());
        prescricao.setInstrucoes(dto.getInstrucoes());

        return toResponseDTO(prescricaoRepository.save(prescricao));
    }
    @Transactional(readOnly = true)
    @Cacheable("prescricoes")
    public Page<PrescricaoResponseDTO> listar(Pageable pageable) {
        return prescricaoRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public PrescricaoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<PrescricaoResponseDTO> buscarPorTratamento(Long idTratamento, Pageable pageable) {
        tratamentoRepository.findById(idTratamento)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tratamento não encontrado com ID: " + idTratamento));
        return prescricaoRepository
                .findByTratamentoIdTratamento(idTratamento, pageable)
                .map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<PrescricaoResponseDTO> buscarPorPet(Long idPet, Pageable pageable) {
        return prescricaoRepository.findByPet(idPet, pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "prescricoes", allEntries = true)
    public PrescricaoResponseDTO atualizar(Long id, PrescricaoRequestDTO dto) {
        Prescricao prescricao = buscarEntidade(id);

        Tratamento tratamento = tratamentoRepository.findById(dto.getIdTratamento())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tratamento não encontrado com ID: " + dto.getIdTratamento()));

        if (!"ATIVO".equals(tratamento.getStatus())) {
            throw new RegraNegocioException("Só é possível alterar prescrições de tratamentos ATIVOS.");
        }

        Medicamento medicamento = medicamentoRepository.findById(dto.getIdMedicamento())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Medicamento não encontrado com ID: " + dto.getIdMedicamento()));

        prescricao.setTratamento(tratamento);
        prescricao.setMedicamento(medicamento);
        prescricao.setDosagem(dto.getDosagem());
        prescricao.setFrequencia(dto.getFrequencia());
        prescricao.setDuracaoDias(dto.getDuracaoDias());
        prescricao.setInstrucoes(dto.getInstrucoes());

        return toResponseDTO(prescricaoRepository.save(prescricao));
    }

    @Transactional
    @CacheEvict(value = "prescricoes", allEntries = true)
    public void deletar(Long id) {
        prescricaoRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Prescricao buscarEntidade(Long id) {
        return prescricaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Prescrição não encontrada com ID: " + id));
    }

    private PrescricaoResponseDTO toResponseDTO(Prescricao p) {
        return PrescricaoResponseDTO.builder()
                .idPrescricao(p.getIdPrescricao())
                .dosagem(p.getDosagem())
                .frequencia(p.getFrequencia())
                .duracaoDias(p.getDuracaoDias())
                .instrucoes(p.getInstrucoes())
                .idTratamento(p.getTratamento().getIdTratamento())
                .descricaoTratamento(p.getTratamento().getDescricao())
                .idMedicamento(p.getMedicamento().getIdMedicamento())
                .nomeMedicamento(p.getMedicamento().getNome())
                .principioAtivo(p.getMedicamento().getPrincipioAtivo())
                .nomePet(p.getTratamento().getPet().getNome())
                .build();
    }
}
