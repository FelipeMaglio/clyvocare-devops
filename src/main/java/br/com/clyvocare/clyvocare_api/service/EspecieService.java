package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.EspecieRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.EspecieResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Especie;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.EspecieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EspecieService {

    private final EspecieRepository especieRepository;

    @Transactional
    @CacheEvict(value = "especies", allEntries = true)
    public EspecieResponseDTO criar(EspecieRequestDTO dto) {
        if (especieRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new RegraNegocioException("Já existe uma espécie cadastrada com o nome: " + dto.getNome());
        }
        Especie especie = new Especie();
        especie.setNome(dto.getNome());
        especie.setDescricao(dto.getDescricao());
        return toResponseDTO(especieRepository.save(especie));
    }
    @Transactional(readOnly = true)
    @Cacheable("especies")
    public Page<EspecieResponseDTO> listar(Pageable pageable) {
        return especieRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public EspecieResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }

    @Transactional
    @CacheEvict(value = "especies", allEntries = true)
    public EspecieResponseDTO atualizar(Long id, EspecieRequestDTO dto) {
        Especie especie = buscarEntidade(id);
        if (especieRepository.existsByNomeIgnoreCase(dto.getNome())
                && !especie.getNome().equalsIgnoreCase(dto.getNome())) {
            throw new RegraNegocioException("Já existe outra espécie com o nome: " + dto.getNome());
        }
        especie.setNome(dto.getNome());
        especie.setDescricao(dto.getDescricao());
        return toResponseDTO(especieRepository.save(especie));
    }

    @Transactional
    @CacheEvict(value = "especies", allEntries = true)
    public void deletar(Long id) {
        especieRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Especie buscarEntidade(Long id) {
        return especieRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Espécie não encontrada com ID: " + id));
    }

    private EspecieResponseDTO toResponseDTO(Especie e) {
        return EspecieResponseDTO.builder()
                .idEspecie(e.getIdEspecie())
                .nome(e.getNome())
                .descricao(e.getDescricao())
                .build();
    }
}
