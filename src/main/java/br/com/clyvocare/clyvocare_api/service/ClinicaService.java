package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.ClinicaRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.ClinicaResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Cidade;
import br.com.clyvocare.clyvocare_api.entity.Clinica;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.CidadeRepository;
import br.com.clyvocare.clyvocare_api.repository.ClinicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicaService {

    private final ClinicaRepository clinicaRepository;
    private final CidadeRepository cidadeRepository;

    @Transactional
    @CacheEvict(value = "clinicas", allEntries = true)
    public ClinicaResponseDTO criar(ClinicaRequestDTO dto) {
        if (clinicaRepository.existsByCnpj(dto.getCnpj())) {
            throw new RegraNegocioException("Já existe uma clínica cadastrada com o CNPJ: " + dto.getCnpj());
        }
        Clinica clinica = new Clinica();
        preencherClinica(clinica, dto);
        return toResponseDTO(clinicaRepository.save(clinica));
    }
    @Transactional(readOnly = true)
    @Cacheable("clinicas")
    public Page<ClinicaResponseDTO> listar(Pageable pageable) {
        return clinicaRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public ClinicaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<ClinicaResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return clinicaRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ClinicaResponseDTO> buscarPorPlano(String plano, Pageable pageable) {
        return clinicaRepository.findByPlanoAssinatura(plano.toUpperCase(), pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ClinicaResponseDTO> buscarPorEstado(String estado, Pageable pageable) {
        return clinicaRepository.findByEstado(estado.toUpperCase(), pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "clinicas", allEntries = true)
    public ClinicaResponseDTO atualizar(Long id, ClinicaRequestDTO dto) {
        Clinica clinica = buscarEntidade(id);
        if (clinicaRepository.existsByCnpjAndIdClinicaNot(dto.getCnpj(), id)) {
            throw new RegraNegocioException("Já existe outra clínica com o CNPJ: " + dto.getCnpj());
        }
        preencherClinica(clinica, dto);
        return toResponseDTO(clinicaRepository.save(clinica));
    }

    @Transactional
    @CacheEvict(value = "clinicas", allEntries = true)
    public void deletar(Long id) {
        Clinica clinica = buscarEntidade(id);
        if (clinica.getVeterinarios() != null && !clinica.getVeterinarios().isEmpty()) {
            throw new RegraNegocioException(
                    "Não é possível excluir a clínica pois ela possui " +
                    clinica.getVeterinarios().size() + " veterinário(s) vinculado(s).");
        }
        clinicaRepository.delete(clinica);
    }
    @Transactional(readOnly = true)
    public Clinica buscarEntidade(Long id) {
        return clinicaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Clínica não encontrada com ID: " + id));
    }

    private void preencherClinica(Clinica clinica, ClinicaRequestDTO dto) {
        clinica.setNome(dto.getNome());
        clinica.setCnpj(dto.getCnpj());
        clinica.setTelefone(dto.getTelefone());
        clinica.setEmail(dto.getEmail());
        clinica.setEndereco(dto.getEndereco());
        clinica.setPlanoAssinatura(dto.getPlanoAssinatura());
        clinica.setDataAssinatura(dto.getDataAssinatura());

        if (dto.getIdCidade() != null) {
            Cidade cidade = cidadeRepository.findById(dto.getIdCidade())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Cidade não encontrada com ID: " + dto.getIdCidade()));
            clinica.setCidade(cidade);
        }
    }

    private ClinicaResponseDTO toResponseDTO(Clinica clinica) {
        return ClinicaResponseDTO.builder()
                .idClinica(clinica.getIdClinica())
                .nome(clinica.getNome())
                .cnpj(clinica.getCnpj())
                .telefone(clinica.getTelefone())
                .email(clinica.getEmail())
                .endereco(clinica.getEndereco())
                .planoAssinatura(clinica.getPlanoAssinatura())
                .dataAssinatura(clinica.getDataAssinatura())
                .nomeCidade(clinica.getCidade() != null ? clinica.getCidade().getNome() : null)
                .estado(clinica.getCidade() != null ? clinica.getCidade().getEstado() : null)
                .build();
    }
}
