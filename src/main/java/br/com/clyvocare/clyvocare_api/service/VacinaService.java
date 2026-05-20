package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.VacinaRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.VacinaResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Especie;
import br.com.clyvocare.clyvocare_api.entity.Vacina;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.EspecieRepository;
import br.com.clyvocare.clyvocare_api.repository.VacinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacinaService {

    private final VacinaRepository vacinaRepository;
    private final EspecieRepository especieRepository;

    @Transactional
    @CacheEvict(value = "vacinas", allEntries = true)
    public VacinaResponseDTO criar(VacinaRequestDTO dto) {
        if (vacinaRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new RegraNegocioException("Já existe uma vacina cadastrada com o nome: " + dto.getNome());
        }
        Vacina vacina = new Vacina();
        preencherVacina(vacina, dto);
        return toResponseDTO(vacinaRepository.save(vacina));
    }
    @Transactional(readOnly = true)
    @Cacheable("vacinas")
    public Page<VacinaResponseDTO> listar(Pageable pageable) {
        return vacinaRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public VacinaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<VacinaResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return vacinaRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<VacinaResponseDTO> buscarPorEspecie(Long idEspecie, Pageable pageable) {
        especieRepository.findById(idEspecie)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Espécie não encontrada com ID: " + idEspecie));
        return vacinaRepository.findByEspecieIdEspecie(idEspecie, pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "vacinas", allEntries = true)
    public VacinaResponseDTO atualizar(Long id, VacinaRequestDTO dto) {
        Vacina vacina = buscarEntidade(id);
        if (vacinaRepository.existsByNomeIgnoreCaseAndIdVacinaNot(dto.getNome(), id)) {
            throw new RegraNegocioException("Já existe outra vacina com o nome: " + dto.getNome());
        }
        preencherVacina(vacina, dto);
        return toResponseDTO(vacinaRepository.save(vacina));
    }

    @Transactional
    @CacheEvict(value = "vacinas", allEntries = true)
    public void deletar(Long id) {
        vacinaRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Vacina buscarEntidade(Long id) {
        return vacinaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Vacina não encontrada com ID: " + id));
    }

    private void preencherVacina(Vacina vacina, VacinaRequestDTO dto) {
        Especie especie = especieRepository.findById(dto.getIdEspecie())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Espécie não encontrada com ID: " + dto.getIdEspecie()));
        vacina.setNome(dto.getNome());
        vacina.setFabricante(dto.getFabricante());
        vacina.setDoencasPrevine(dto.getDoencasPrevine());
        vacina.setIntervaloReforcoDias(dto.getIntervaloReforcoDias());
        vacina.setEspecie(especie);
    }

    private VacinaResponseDTO toResponseDTO(Vacina v) {
        return VacinaResponseDTO.builder()
                .idVacina(v.getIdVacina())
                .nome(v.getNome())
                .fabricante(v.getFabricante())
                .doencasPrevine(v.getDoencasPrevine())
                .intervaloReforcoDias(v.getIntervaloReforcoDias())
                .idEspecie(v.getEspecie().getIdEspecie())
                .nomeEspecie(v.getEspecie().getNome())
                .build();
    }
}
