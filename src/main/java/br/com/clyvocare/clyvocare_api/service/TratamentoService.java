package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.TratamentoRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.TratamentoResponseDTO;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TratamentoService {

    private final TratamentoRepository tratamentoRepository;
    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;

    @Transactional
    @CacheEvict(value = "tratamentos", allEntries = true)
    public TratamentoResponseDTO criar(TratamentoRequestDTO dto) {
        // Regra: data_fim não pode ser antes da data_inicio
        if (dto.getDataFim() != null && dto.getDataFim().isBefore(dto.getDataInicio())) {
            throw new RegraNegocioException("A data de término não pode ser anterior à data de início.");
        }
        Tratamento tratamento = new Tratamento();
        preencherTratamento(tratamento, dto);
        return toResponseDTO(tratamentoRepository.save(tratamento));
    }
    @Transactional(readOnly = true)
    @Cacheable("tratamentos")
    public Page<TratamentoResponseDTO> listar(Pageable pageable) {
        return tratamentoRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public TratamentoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<TratamentoResponseDTO> buscarPorPet(Long idPet, Pageable pageable) {
        petRepository.findById(idPet)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + idPet));
        return tratamentoRepository
                .findByPetIdPetOrderByDataInicioDesc(idPet, pageable)
                .map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<TratamentoResponseDTO> buscarAtivos(Pageable pageable) {
        return tratamentoRepository.findByStatus("ATIVO", pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "tratamentos", allEntries = true)
    public TratamentoResponseDTO atualizar(Long id, TratamentoRequestDTO dto) {
        Tratamento tratamento = buscarEntidade(id);

        if (dto.getDataFim() != null && dto.getDataFim().isBefore(dto.getDataInicio())) {
            throw new RegraNegocioException("A data de término não pode ser anterior à data de início.");
        }
        // Regra: tratamento concluído não pode voltar para ativo
        if ("CONCLUIDO".equals(tratamento.getStatus()) && "ATIVO".equals(dto.getStatus())) {
            throw new RegraNegocioException("Um tratamento já concluído não pode ser reativado.");
        }

        preencherTratamento(tratamento, dto);
        return toResponseDTO(tratamentoRepository.save(tratamento));
    }

    @Transactional
    @CacheEvict(value = "tratamentos", allEntries = true)
    public void deletar(Long id) {
        Tratamento tratamento = buscarEntidade(id);
        if ("ATIVO".equals(tratamento.getStatus())) {
            throw new RegraNegocioException("Não é possível excluir um tratamento ativo. Conclua ou suspenda primeiro.");
        }
        tratamentoRepository.delete(tratamento);
    }
    @Transactional(readOnly = true)
    public Tratamento buscarEntidade(Long id) {
        return tratamentoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tratamento não encontrado com ID: " + id));
    }

    private void preencherTratamento(Tratamento t, TratamentoRequestDTO dto) {
        Pet pet = petRepository.findById(dto.getIdPet())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + dto.getIdPet()));

        t.setPet(pet);
        t.setDescricao(dto.getDescricao());
        t.setDataInicio(dto.getDataInicio());
        t.setDataFim(dto.getDataFim());
        t.setStatus(dto.getStatus());

        if (dto.getIdConsulta() != null) {
            Consulta consulta = consultaRepository.findById(dto.getIdConsulta())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Consulta não encontrada com ID: " + dto.getIdConsulta()));
            t.setConsulta(consulta);
        }
    }

    private TratamentoResponseDTO toResponseDTO(Tratamento t) {
        long diasEmAndamento = ChronoUnit.DAYS.between(t.getDataInicio(), LocalDate.now());
        boolean abandonado = "ATIVO".equals(t.getStatus()) && diasEmAndamento > 30;

        return TratamentoResponseDTO.builder()
                .idTratamento(t.getIdTratamento())
                .descricao(t.getDescricao())
                .dataInicio(t.getDataInicio())
                .dataFim(t.getDataFim())
                .status(t.getStatus())
                .diasEmAndamento(diasEmAndamento)
                .abandonado(abandonado)
                .idPet(t.getPet().getIdPet())
                .nomePet(t.getPet().getNome())
                .build();
    }
}
