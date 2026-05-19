package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.FaseVidaResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.FaseVida;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.repository.EspecieRepository;
import br.com.clyvocare.clyvocare_api.repository.FaseVidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaseVidaService {

    private final FaseVidaRepository faseVidaRepository;
    private final EspecieRepository especieRepository;
    @Transactional(readOnly = true)
    @Cacheable("fases")
    public Page<FaseVidaResponseDTO> listar(Pageable pageable) {
        return faseVidaRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public FaseVidaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(faseVidaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fase de vida não encontrada com ID: " + id)));
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "fases", key = "#idEspecie")
    public Page<FaseVidaResponseDTO> buscarPorEspecie(Long idEspecie, Pageable pageable) {
        especieRepository.findById(idEspecie)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Espécie não encontrada com ID: " + idEspecie));
        return faseVidaRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    private FaseVidaResponseDTO toResponseDTO(FaseVida f) {
        return FaseVidaResponseDTO.builder()
                .idFase(f.getIdFase())
                .nome(f.getNome())
                .idadeMinMeses(f.getIdadeMinMeses())
                .idadeMaxMeses(f.getIdadeMaxMeses())
                .idEspecie(f.getEspecie().getIdEspecie())
                .nomeEspecie(f.getEspecie().getNome())
                .build();
    }
}
