package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.VacinacaoRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.VacinacaoResponseDTO;
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
public class VacinacaoService {

    private final VacinacaoRepository vacinacaoRepository;
    private final PetRepository petRepository;
    private final VacinaRepository vacinaRepository;
    private final ConsultaRepository consultaRepository;
    private final VeterinarioRepository veterinarioRepository;

    @Transactional
    @CacheEvict(value = "vacinacoes", allEntries = true)
    public VacinacaoResponseDTO criar(VacinacaoRequestDTO dto) {
        // Regra: data aplicação não pode ser futura
        if (dto.getDataAplicacao().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de aplicação não pode ser uma data futura.");
        }
        Vacinacao vacinacao = new Vacinacao();
        preencherVacinacao(vacinacao, dto);
        return toResponseDTO(vacinacaoRepository.save(vacinacao));
    }
    @Transactional(readOnly = true)
    @Cacheable("vacinacoes")
    public Page<VacinacaoResponseDTO> listar(Pageable pageable) {
        return vacinacaoRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public VacinacaoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<VacinacaoResponseDTO> buscarPorPet(Long idPet, Pageable pageable) {
        petRepository.findById(idPet)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + idPet));
        return vacinacaoRepository
                .findByPetIdPetOrderByDataAplicacaoDesc(idPet, pageable)
                .map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<VacinacaoResponseDTO> buscarAtrasadasPorPet(Long idPet, Pageable pageable) {
        petRepository.findById(idPet)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + idPet));
        return vacinacaoRepository.findVacinasAtrasadasByPet(idPet, pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "vacinacoes", allEntries = true)
    public VacinacaoResponseDTO atualizar(Long id, VacinacaoRequestDTO dto) {
        if (dto.getDataAplicacao().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de aplicação não pode ser uma data futura.");
        }
        Vacinacao vacinacao = buscarEntidade(id);
        preencherVacinacao(vacinacao, dto);
        return toResponseDTO(vacinacaoRepository.save(vacinacao));
    }

    @Transactional
    @CacheEvict(value = "vacinacoes", allEntries = true)
    public void deletar(Long id) {
        vacinacaoRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Vacinacao buscarEntidade(Long id) {
        return vacinacaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Vacinação não encontrada com ID: " + id));
    }

    private void preencherVacinacao(Vacinacao v, VacinacaoRequestDTO dto) {
        Pet pet = petRepository.findById(dto.getIdPet())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + dto.getIdPet()));

        Vacina vacina = vacinaRepository.findById(dto.getIdVacina())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Vacina não encontrada com ID: " + dto.getIdVacina()));

        // Regra: vacina deve ser compatível com a espécie do pet
        if (!vacina.getEspecie().getIdEspecie().equals(pet.getEspecie().getIdEspecie())) {
            throw new RegraNegocioException("A vacina informada não é compatível com a espécie do pet.");
        }

        v.setPet(pet);
        v.setVacina(vacina);
        v.setDataAplicacao(dto.getDataAplicacao());
        v.setLote(dto.getLote());
        v.setProximaDose(dto.getProximaDose());
        v.setValidade(dto.getValidade());

        if (dto.getIdConsulta() != null) {
            Consulta consulta = consultaRepository.findById(dto.getIdConsulta())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Consulta não encontrada com ID: " + dto.getIdConsulta()));
            v.setConsulta(consulta);
        }

        if (dto.getIdVeterinario() != null) {
            Veterinario vet = veterinarioRepository.findById(dto.getIdVeterinario())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Veterinário não encontrado com ID: " + dto.getIdVeterinario()));
            v.setVeterinario(vet);
        }
    }

    private VacinacaoResponseDTO toResponseDTO(Vacinacao v) {
        boolean atrasada = v.getProximaDose() != null && v.getProximaDose().isBefore(LocalDate.now());
        long diasAtraso = atrasada ? ChronoUnit.DAYS.between(v.getProximaDose(), LocalDate.now()) : 0;

        return VacinacaoResponseDTO.builder()
                .idVacinacao(v.getIdVacinacao())
                .dataAplicacao(v.getDataAplicacao())
                .lote(v.getLote())
                .proximaDose(v.getProximaDose())
                .validade(v.getValidade())
                .atrasada(atrasada)
                .diasAtraso(diasAtraso)
                .idPet(v.getPet().getIdPet())
                .nomePet(v.getPet().getNome())
                .nomeVacina(v.getVacina().getNome())
                .nomeVeterinario(v.getVeterinario() != null ? v.getVeterinario().getNome() : null)
                .build();
    }
}
