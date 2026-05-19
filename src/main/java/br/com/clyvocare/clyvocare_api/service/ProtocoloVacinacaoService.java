package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.ProtocoloVacinacaoRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.ProtocoloVacinacaoResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.FaseVida;
import br.com.clyvocare.clyvocare_api.entity.ProtocoloVacinacao;
import br.com.clyvocare.clyvocare_api.entity.Vacina;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.repository.FaseVidaRepository;
import br.com.clyvocare.clyvocare_api.repository.ProtocoloVacinacaoRepository;
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
public class ProtocoloVacinacaoService {

    private final ProtocoloVacinacaoRepository protocoloRepository;
    private final VacinaRepository vacinaRepository;
    private final FaseVidaRepository faseVidaRepository;

    @Transactional
    @CacheEvict(value = "protocolos", allEntries = true)
    public ProtocoloVacinacaoResponseDTO criar(ProtocoloVacinacaoRequestDTO dto) {
        Vacina vacina = vacinaRepository.findById(dto.getIdVacina())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Vacina não encontrada com ID: " + dto.getIdVacina()));

        FaseVida fase = faseVidaRepository.findById(dto.getIdFase())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fase de vida não encontrada com ID: " + dto.getIdFase()));

        ProtocoloVacinacao protocolo = new ProtocoloVacinacao();
        protocolo.setVacina(vacina);
        protocolo.setFaseVida(fase);
        protocolo.setObrigatoria(dto.getObrigatoria());
        protocolo.setIdadeAplicacaoDias(dto.getIdadeAplicacaoDias());

        return toResponseDTO(protocoloRepository.save(protocolo));
    }
    @Transactional(readOnly = true)
    @Cacheable("protocolos")
    public Page<ProtocoloVacinacaoResponseDTO> listar(Pageable pageable) {
        return protocoloRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public ProtocoloVacinacaoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<ProtocoloVacinacaoResponseDTO> buscarPorFase(Long idFase, Pageable pageable) {
        return protocoloRepository.findByFaseVidaIdFase(idFase, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<ProtocoloVacinacaoResponseDTO> buscarPorEspecie(Long idEspecie, Pageable pageable) {
        return protocoloRepository.findByEspecie(idEspecie, pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "protocolos", allEntries = true)
    public void deletar(Long id) {
        protocoloRepository.delete(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    private ProtocoloVacinacao buscarEntidade(Long id) {
        return protocoloRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Protocolo não encontrado com ID: " + id));
    }

    private ProtocoloVacinacaoResponseDTO toResponseDTO(ProtocoloVacinacao p) {
        return ProtocoloVacinacaoResponseDTO.builder()
                .idProtocolo(p.getIdProtocolo())
                .obrigatoria(p.getObrigatoria())
                .idadeAplicacaoDias(p.getIdadeAplicacaoDias())
                .idVacina(p.getVacina().getIdVacina())
                .nomeVacina(p.getVacina().getNome())
                .idFase(p.getFaseVida().getIdFase())
                .nomeFase(p.getFaseVida().getNome())
                .nomeEspecie(p.getFaseVida().getEspecie().getNome())
                .build();
    }
}
