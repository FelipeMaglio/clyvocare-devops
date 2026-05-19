package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.AlertaRegionalResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.AlertaRegional;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.repository.AlertaRegionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AlertaRegionalService {

    private final AlertaRegionalRepository alertaRepository;
    @Transactional(readOnly = true)
    @Cacheable("alertas-regionais")
    public Page<AlertaRegionalResponseDTO> listar(Pageable pageable) {
        return alertaRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public AlertaRegionalResponseDTO buscarPorId(Long id) {
        return toResponseDTO(alertaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Alerta não encontrado com ID: " + id)));
    }
    @Transactional(readOnly = true)
    @Cacheable("alertas-ativos")
    public Page<AlertaRegionalResponseDTO> buscarAtivos(Pageable pageable) {
        return alertaRepository.findAtivos(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<AlertaRegionalResponseDTO> buscarPorEstado(String estado, Pageable pageable) {
        return alertaRepository.findAtivosByEstado(estado.toUpperCase(), pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<AlertaRegionalResponseDTO> buscarPorCidade(Long idCidade, Pageable pageable) {
        return alertaRepository.findAtivosByCidade(idCidade, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<AlertaRegionalResponseDTO> buscarPorNivel(String nivel, Pageable pageable) {
        return alertaRepository.findByNivelRisco(nivel.toUpperCase(), pageable).map(this::toResponseDTO);
    }

    private AlertaRegionalResponseDTO toResponseDTO(AlertaRegional a) {
        boolean ativo = a.getDataFim() == null || !a.getDataFim().isBefore(LocalDate.now());
        return AlertaRegionalResponseDTO.builder()
                .idAlerta(a.getIdAlerta())
                .doenca(a.getDoenca())
                .nivelRisco(a.getNivelRisco())
                .dataInicio(a.getDataInicio())
                .dataFim(a.getDataFim())
                .ativo(ativo)
                .descricao(a.getDescricao())
                .fonte(a.getFonte())
                .idCidade(a.getCidade().getIdCidade())
                .nomeCidade(a.getCidade().getNome())
                .estado(a.getCidade().getEstado())
                .regiao(a.getCidade().getRegiao())
                .build();
    }
}
