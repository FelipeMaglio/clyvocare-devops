package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.RacaRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.RacaResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Especie;
import br.com.clyvocare.clyvocare_api.entity.Raca;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.repository.EspecieRepository;
import br.com.clyvocare.clyvocare_api.repository.RacaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RacaService {

    private final RacaRepository racaRepository;
    private final EspecieRepository especieRepository;

    @Transactional
    @CacheEvict(value = "racas", allEntries = true)
    public RacaResponseDTO criar(RacaRequestDTO dto) {
        Raca raca = new Raca();
        preencherRaca(raca, dto);
        return toResponseDTO(racaRepository.save(raca));
    }

    @Transactional(readOnly = true)
    @Cacheable("racas")
    public Page<RacaResponseDTO> listar(Pageable pageable) {
        return racaRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public RacaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<RacaResponseDTO> buscarPorEspecie(Long idEspecie, Pageable pageable) {
        especieRepository.findById(idEspecie)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Espécie não encontrada com ID: " + idEspecie));
        return racaRepository.findByEspecieIdEspecie(idEspecie, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<RacaResponseDTO> buscarPorPorte(String porte, Pageable pageable) {
        return racaRepository.findByPorteIgnoreCase(porte, pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "racas", allEntries = true)
    public RacaResponseDTO atualizar(Long id, RacaRequestDTO dto) {
        Raca raca = buscarEntidade(id);
        preencherRaca(raca, dto);
        return toResponseDTO(racaRepository.save(raca));
    }

    @Transactional
    @CacheEvict(value = "racas", allEntries = true)
    public void deletar(Long id) {
        racaRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Raca buscarEntidade(Long id) {
        return racaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Raça não encontrada com ID: " + id));
    }

    private void preencherRaca(Raca raca, RacaRequestDTO dto) {
        Especie especie = especieRepository.findById(dto.getIdEspecie())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Espécie não encontrada com ID: " + dto.getIdEspecie()));
        raca.setNome(dto.getNome());
        raca.setPorte(dto.getPorte());
        raca.setEspecie(especie);
    }

    private RacaResponseDTO toResponseDTO(Raca r) {
        return RacaResponseDTO.builder()
                .idRaca(r.getIdRaca())
                .nome(r.getNome())
                .porte(r.getPorte())
                .idEspecie(r.getEspecie().getIdEspecie())
                .nomeEspecie(r.getEspecie().getNome())
                .build();
    }
}
