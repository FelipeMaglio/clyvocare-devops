package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.VeterinarioRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.VeterinarioResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Clinica;
import br.com.clyvocare.clyvocare_api.entity.Veterinario;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.ClinicaRepository;
import br.com.clyvocare.clyvocare_api.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;

    @Transactional
    @CacheEvict(value = "veterinarios", allEntries = true)
    public VeterinarioResponseDTO criar(VeterinarioRequestDTO dto) {
        if (veterinarioRepository.existsByCrmv(dto.getCrmv())) {
            throw new RegraNegocioException("Já existe um veterinário cadastrado com o CRMV: " + dto.getCrmv());
        }
        Veterinario vet = new Veterinario();
        preencherVeterinario(vet, dto);
        return toResponseDTO(veterinarioRepository.save(vet));
    }
    @Transactional(readOnly = true)
    @Cacheable("veterinarios")
    public Page<VeterinarioResponseDTO> listar(Pageable pageable) {
        return veterinarioRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public VeterinarioResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<VeterinarioResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return veterinarioRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<VeterinarioResponseDTO> buscarPorEspecialidade(String especialidade, Pageable pageable) {
        return veterinarioRepository.findByEspecialidadeContainingIgnoreCase(especialidade, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<VeterinarioResponseDTO> buscarPorClinica(Long idClinica, Pageable pageable) {
        clinicaRepository.findById(idClinica)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Clínica não encontrada com ID: " + idClinica));
        return veterinarioRepository.findByClinica(idClinica, pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "veterinarios", allEntries = true)
    public VeterinarioResponseDTO atualizar(Long id, VeterinarioRequestDTO dto) {
        Veterinario vet = buscarEntidade(id);
        if (veterinarioRepository.existsByCrmvAndIdVeterinarioNot(dto.getCrmv(), id)) {
            throw new RegraNegocioException("Já existe outro veterinário com o CRMV: " + dto.getCrmv());
        }
        preencherVeterinario(vet, dto);
        return toResponseDTO(veterinarioRepository.save(vet));
    }

    @Transactional
    @CacheEvict(value = "veterinarios", allEntries = true)
    public void deletar(Long id) {
        Veterinario vet = buscarEntidade(id);
        if (vet.getConsultas() != null && !vet.getConsultas().isEmpty()) {
            throw new RegraNegocioException(
                    "Não é possível excluir o veterinário pois ele possui consultas registradas.");
        }
        veterinarioRepository.delete(vet);
    }
    @Transactional(readOnly = true)
    public Veterinario buscarEntidade(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Veterinário não encontrado com ID: " + id));
    }

    private void preencherVeterinario(Veterinario vet, VeterinarioRequestDTO dto) {
        vet.setNome(dto.getNome());
        vet.setCrmv(dto.getCrmv());
        vet.setEspecialidade(dto.getEspecialidade());
        vet.setEmail(dto.getEmail());
        vet.setTelefone(dto.getTelefone());

        if (dto.getIdClinica() != null) {
            Clinica clinica = clinicaRepository.findById(dto.getIdClinica())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Clínica não encontrada com ID: " + dto.getIdClinica()));
            vet.setClinica(clinica);
        } else {
            vet.setClinica(null);
        }
    }

    private VeterinarioResponseDTO toResponseDTO(Veterinario vet) {
        return VeterinarioResponseDTO.builder()
                .idVeterinario(vet.getIdVeterinario())
                .nome(vet.getNome())
                .crmv(vet.getCrmv())
                .especialidade(vet.getEspecialidade())
                .email(vet.getEmail())
                .telefone(vet.getTelefone())
                .idClinica(vet.getClinica() != null ? vet.getClinica().getIdClinica() : null)
                .nomeClinica(vet.getClinica() != null ? vet.getClinica().getNome() : null)
                .build();
    }
}
