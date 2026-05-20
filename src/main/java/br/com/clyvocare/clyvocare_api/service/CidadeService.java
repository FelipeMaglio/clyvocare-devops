package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.CidadeResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Cidade;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.repository.CidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CidadeService {

    private final CidadeRepository cidadeRepository;
    @Transactional(readOnly = true)
    @Cacheable("cidades")
    public Page<CidadeResponseDTO> listar(Pageable pageable) {
        return cidadeRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public CidadeResponseDTO buscarPorId(Long id) {
        return toResponseDTO(cidadeRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cidade não encontrada com ID: " + id)));
    }
    @Transactional(readOnly = true)
    public Page<CidadeResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return cidadeRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<CidadeResponseDTO> buscarPorEstado(String estado, Pageable pageable) {
        return cidadeRepository.findByEstado(estado.toUpperCase(), pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<CidadeResponseDTO> buscarPorRegiao(String regiao, Pageable pageable) {
        return cidadeRepository.findByRegiao(regiao, pageable).map(this::toResponseDTO);
    }

    private CidadeResponseDTO toResponseDTO(Cidade c) {
        return CidadeResponseDTO.builder()
                .idCidade(c.getIdCidade())
                .nome(c.getNome())
                .estado(c.getEstado())
                .regiao(c.getRegiao())
                .build();
    }
}
