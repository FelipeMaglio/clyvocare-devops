package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.MedicamentoRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.MedicamentoResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Medicamento;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    @Transactional
    @CacheEvict(value = "medicamentos", allEntries = true)
    public MedicamentoResponseDTO criar(MedicamentoRequestDTO dto) {
        if (medicamentoRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new RegraNegocioException("Já existe um medicamento cadastrado com o nome: " + dto.getNome());
        }
        Medicamento med = new Medicamento();
        preencherMedicamento(med, dto);
        return toResponseDTO(medicamentoRepository.save(med));
    }
    @Transactional(readOnly = true)
    @Cacheable("medicamentos")
    public Page<MedicamentoResponseDTO> listar(Pageable pageable) {
        return medicamentoRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public MedicamentoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }

    public Page<MedicamentoResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return medicamentoRepository
                .findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::toResponseDTO);
    }

    public Page<MedicamentoResponseDTO> buscarPorPrincipioAtivo(String principioAtivo, Pageable pageable) {
        return medicamentoRepository
                .findByPrincipioAtivoContainingIgnoreCase(principioAtivo, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "medicamentos", allEntries = true)
    public MedicamentoResponseDTO atualizar(Long id, MedicamentoRequestDTO dto) {
        Medicamento med = buscarEntidade(id);
        if (medicamentoRepository.existsByNomeIgnoreCaseAndIdMedicamentoNot(dto.getNome(), id)) {
            throw new RegraNegocioException("Já existe outro medicamento com o nome: " + dto.getNome());
        }
        preencherMedicamento(med, dto);
        return toResponseDTO(medicamentoRepository.save(med));
    }

    @Transactional
    @CacheEvict(value = "medicamentos", allEntries = true)
    public void deletar(Long id) {
        medicamentoRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Medicamento buscarEntidade(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Medicamento não encontrado com ID: " + id));
    }

    private void preencherMedicamento(Medicamento med, MedicamentoRequestDTO dto) {
        med.setNome(dto.getNome());
        med.setPrincipioAtivo(dto.getPrincipioAtivo());
        med.setFabricante(dto.getFabricante());
    }

    private MedicamentoResponseDTO toResponseDTO(Medicamento med) {
        return MedicamentoResponseDTO.builder()
                .idMedicamento(med.getIdMedicamento())
                .nome(med.getNome())
                .principioAtivo(med.getPrincipioAtivo())
                .fabricante(med.getFabricante())
                .build();
    }
}
