package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.HistoricoPesoRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.HistoricoPesoResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Consulta;
import br.com.clyvocare.clyvocare_api.entity.HistoricoPeso;
import br.com.clyvocare.clyvocare_api.entity.Pet;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.ConsultaRepository;
import br.com.clyvocare.clyvocare_api.repository.HistoricoPesoRepository;
import br.com.clyvocare.clyvocare_api.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HistoricoPesoService {

    private final HistoricoPesoRepository historicoPesoRepository;
    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;

    @Transactional
    public HistoricoPesoResponseDTO registrar(HistoricoPesoRequestDTO dto) {
        // Regra: data não pode ser futura
        if (dto.getDataRegistro().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de registro não pode ser uma data futura.");
        }

        Pet pet = petRepository.findById(dto.getIdPet())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + dto.getIdPet()));

        HistoricoPeso historico = new HistoricoPeso();
        historico.setPet(pet);
        historico.setDataRegistro(dto.getDataRegistro());
        historico.setPeso(dto.getPeso());

        if (dto.getIdConsulta() != null) {
            Consulta consulta = consultaRepository.findById(dto.getIdConsulta())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Consulta não encontrada com ID: " + dto.getIdConsulta()));
            historico.setConsulta(consulta);
        }

        HistoricoPeso salvo = historicoPesoRepository.save(historico);

        // Atualiza o peso atual do pet
        pet.setPesoAtual(dto.getPeso());
        petRepository.save(pet);

        return toResponseDTO(salvo);
    }
    @Transactional(readOnly = true)
    public Page<HistoricoPesoResponseDTO> buscarPorPet(Long idPet, Pageable pageable) {
        petRepository.findById(idPet)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + idPet));
        return historicoPesoRepository
                .findByPetIdPetOrderByDataRegistroDesc(idPet, pageable)
                .map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public HistoricoPesoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }

    @Transactional
    public void deletar(Long id) {
        historicoPesoRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    private HistoricoPeso buscarEntidade(Long id) {
        return historicoPesoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Registro de peso não encontrado com ID: " + id));
    }

    private HistoricoPesoResponseDTO toResponseDTO(HistoricoPeso h) {
        // Calcula variação em relação ao registro anterior
        BigDecimal variacao = null;
        Page<HistoricoPeso> anteriores = historicoPesoRepository.findPesoAnterior(
                h.getPet().getIdPet(), h.getIdHistorico(), PageRequest.of(0, 1));

        if (!anteriores.isEmpty()) {
            variacao = h.getPeso().subtract(anteriores.getContent().get(0).getPeso());
        }

        return HistoricoPesoResponseDTO.builder()
                .idHistorico(h.getIdHistorico())
                .dataRegistro(h.getDataRegistro())
                .peso(h.getPeso())
                .variacao(variacao)
                .idPet(h.getPet().getIdPet())
                .nomePet(h.getPet().getNome())
                .idConsulta(h.getConsulta() != null ? h.getConsulta().getIdConsulta() : null)
                .build();
    }
}
