package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.PetRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.AlertaResponseDTO;
import br.com.clyvocare.clyvocare_api.dto.PetResponseDTO;
import br.com.clyvocare.clyvocare_api.dto.RecomendacaoResponseDTO;
import br.com.clyvocare.clyvocare_api.dto.RiscoResponseDTO;
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
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final EspecieRepository especieRepository;
    private final RacaRepository racaRepository;
    private final FaseVidaRepository faseVidaRepository;
    private final VacinacaoRepository vacinacaoRepository;
    private final ConsultaRepository consultaRepository;
    private final TratamentoRepository tratamentoRepository;


    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetResponseDTO criar(PetRequestDTO dto) {
        if (dto.getMicrochip() != null && petRepository.existsByMicrochip(dto.getMicrochip())) {
            throw new RegraNegocioException("Já existe um pet cadastrado com o microchip: " + dto.getMicrochip());
        }

        Pet pet = new Pet();
        preencherPet(pet, dto);
        pet.setDataCadastro(LocalDate.now());

        return toResponseDTO(petRepository.save(pet));
    }

    @Transactional(readOnly = true)
    @Cacheable("pets")
    public Page<PetResponseDTO> listar(Pageable pageable) {
        return petRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public PetResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> buscarPorTutor(Long idTutor, Pageable pageable) {
        tutorRepository.findById(idTutor)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tutor não encontrado com ID: " + idTutor));
        return petRepository.findByTutorIdTutor(idTutor, pageable).map(this::toResponseDTO);
    }


    @Transactional(readOnly = true)
    public Page<PetResponseDTO> buscarPorEspecie(String nomeEspecie, Pageable pageable) {
        return petRepository.findByEspecie(nomeEspecie, pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return petRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponseDTO);
    }


    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetResponseDTO atualizar(Long id, PetRequestDTO dto) {
        Pet pet = buscarEntidade(id);

        if (dto.getMicrochip() != null
                && petRepository.existsByMicrochipAndIdPetNot(dto.getMicrochip(), id)) {
            throw new RegraNegocioException("Já existe outro pet com o microchip: " + dto.getMicrochip());
        }

        preencherPet(pet, dto);
        return toResponseDTO(petRepository.save(pet));
    }


    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public void deletar(Long id) {
        Pet pet = buscarEntidade(id);

        boolean temConsultas   = consultaRepository.existsByPetIdPet(id);
        boolean temTratamentos = tratamentoRepository.existsByPetIdPet(id);

        if (temConsultas || temTratamentos) {
            throw new RegraNegocioException(
                    "Não é possível excluir o pet pois ele possui histórico clínico (consultas/tratamentos).");
        }
        petRepository.delete(pet);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "risco", key = "#id")
    public RiscoResponseDTO calcularRisco(Long id) {
        Pet pet = buscarEntidade(id);

        int pontuacao = 0;
        List<String> fatores = new ArrayList<>();

        // vacinas atrasadas
        int vacinasAtrasadas = vacinacaoRepository.countVacinasAtrasadas(id);
        if (vacinasAtrasadas > 0) {
            pontuacao += vacinasAtrasadas * 20;
            fatores.add(vacinasAtrasadas + " vacina(s) atrasada(s)");
        }

        // tratamentos abandonados (ativos há mais de 30 dias)
        int tratamentosAbandonados = tratamentoRepository.countTratamentosAbandonados(id);
        if (tratamentosAbandonados > 0) {
            pontuacao += tratamentosAbandonados * 25;
            fatores.add(tratamentosAbandonados + " tratamento(s) sem conclusão há mais de 30 dias");
        }

        //  sem consulta há mais de 12 meses
        int mesesSemConsulta = consultaRepository.mesesDesdeUltimaConsulta(id);
        if (mesesSemConsulta > 12) {
            pontuacao += 20;
            fatores.add("Sem consulta há " + mesesSemConsulta + " meses");
        }

        //  pet idoso
        int idadeMeses = calcularIdadeMeses(pet);
        String faseVida = calcularFaseVida(pet, idadeMeses);
        if ("Idoso".equalsIgnoreCase(faseVida)) {
            pontuacao += 10;
            fatores.add("Pet em fase idosa requer atenção redobrada");
        }

        // Normaliza em 100
        pontuacao = Math.min(pontuacao, 100);

        String nivel;
        if (pontuacao <= 30)      nivel = "BAIXO";
        else if (pontuacao <= 60) nivel = "MEDIO";
        else                      nivel = "ALTO";

        return RiscoResponseDTO.builder()
                .idPet(id)
                .nomePet(pet.getNome())
                .nivelRisco(nivel)
                .pontuacao(pontuacao)
                .fatoresRisco(fatores)
                .vacinasAtrasadas(vacinasAtrasadas)
                .tratamentosAbandonados(tratamentosAbandonados)
                .mesesSemConsulta(mesesSemConsulta)
                .build();
    }
    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> gerarAlertas(Long id) {
        Pet pet = buscarEntidade(id);
        List<AlertaResponseDTO> alertas = new ArrayList<>();

        //  vacinas atrasadas
        int vacinasAtrasadas = vacinacaoRepository.countVacinasAtrasadas(id);
        if (vacinasAtrasadas > 0) {
            alertas.add(AlertaResponseDTO.builder()
                    .tipo("VACINA_ATRASADA")
                    .urgencia("ALTO")
                    .titulo(vacinasAtrasadas + " vacina(s) atrasada(s)")
                    .descricao("O pet " + pet.getNome() + " possui " + vacinasAtrasadas +
                               " vacina(s) com a data de reforço vencida.")
                    .acaoRecomendada("Agendar vacinação com urgência")
                    .build());
        }

        //  tratamento abandonado
        int tratamentosAbandonados = tratamentoRepository.countTratamentosAbandonados(id);
        if (tratamentosAbandonados > 0) {
            alertas.add(AlertaResponseDTO.builder()
                    .tipo("TRATAMENTO_ABANDONADO")
                    .urgencia("MEDIO")
                    .titulo("Tratamento em aberto há mais de 30 dias")
                    .descricao("Há " + tratamentosAbandonados + " tratamento(s) ativo(s) sem conclusão.")
                    .acaoRecomendada("Verificar andamento do tratamento com o veterinário")
                    .build());
        }

        //  sem consulta há mais de 12 meses
        int mesesSemConsulta = consultaRepository.mesesDesdeUltimaConsulta(id);
        if (mesesSemConsulta > 12) {
            alertas.add(AlertaResponseDTO.builder()
                    .tipo("CHECKUP_PENDENTE")
                    .urgencia("MEDIO")
                    .titulo("Check-up anual pendente")
                    .descricao("O pet está há " + mesesSemConsulta + " meses sem consulta realizada.")
                    .acaoRecomendada("Agendar consulta de rotina")
                    .build());
        }

        //  pet idoso sem consulta há 6 meses
        int idadeMeses = calcularIdadeMeses(pet);
        String faseVida = calcularFaseVida(pet, idadeMeses);
        if ("Idoso".equalsIgnoreCase(faseVida) && mesesSemConsulta > 6) {
            alertas.add(AlertaResponseDTO.builder()
                    .tipo("CHECKUP_GERIATRICO")
                    .urgencia("ALTO")
                    .titulo("Check-up geriátrico recomendado")
                    .descricao("Pets idosos necessitam de acompanhamento semestral. " +
                               pet.getNome() + " está há " + mesesSemConsulta + " meses sem consulta.")
                    .acaoRecomendada("Agendar consulta geriátrica com urgência")
                    .build());
        }

        return alertas;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "recomendacoes", key = "#id")
    public RecomendacaoResponseDTO gerarRecomendacoes(Long id) {
        Pet pet = buscarEntidade(id);
        int idadeMeses = calcularIdadeMeses(pet);
        String faseVida = calcularFaseVida(pet, idadeMeses);
        String especie = pet.getEspecie() != null ? pet.getEspecie().getNome().toLowerCase() : "";

        List<String> recomendacoes = new ArrayList<>();

        if (faseVida.equalsIgnoreCase("Filhote")) {
            if (especie.contains("cão") || especie.contains("cao") || especie.contains("cachorro")) {
                recomendacoes.add("Completar esquema vacinal (V8/V10)");
                recomendacoes.add("Vermifugação a cada 15 dias até 3 meses");
                recomendacoes.add("Iniciar socialização e adestramento básico");
                recomendacoes.add("Consulta de acompanhamento mensal");
            } else if (especie.contains("gato") || especie.contains("felino")) {
                recomendacoes.add("Vacina quádrupla felina (V4)");
                recomendacoes.add("Avaliar castração a partir de 6 meses");
                recomendacoes.add("Vermifugação inicial");
                recomendacoes.add("Consulta de acompanhamento mensal");
            } else {
                recomendacoes.add("Consultar protocolo vacinal específico da espécie");
                recomendacoes.add("Acompanhamento nutricional para crescimento saudável");
            }
        } else if (faseVida.equalsIgnoreCase("Adulto")) {
            if (especie.contains("cão") || especie.contains("cao") || especie.contains("cachorro")) {
                recomendacoes.add("Reforço vacinal anual (V8/V10 + antirrábica)");
                recomendacoes.add("Antiparasitário mensal (pulgas, carrapatos, vermes)");
                recomendacoes.add("Check-up anual com hemograma");
                recomendacoes.add("Controle de peso e dieta balanceada");
                recomendacoes.add("Limpeza dental semestral");
            } else if (especie.contains("gato") || especie.contains("felino")) {
                recomendacoes.add("Reforço vacinal anual");
                recomendacoes.add("Controle de parasitas internos e externos");
                recomendacoes.add("Avaliação renal anual (gatos são propensos a doença renal)");
                recomendacoes.add("Enriquecimento ambiental e estímulo mental");
            } else {
                recomendacoes.add("Check-up anual preventivo");
                recomendacoes.add("Manter vacinação em dia conforme protocolo");
            }
        } else if (faseVida.equalsIgnoreCase("Idoso")) {
            if (especie.contains("cão") || especie.contains("cao") || especie.contains("cachorro")) {
                recomendacoes.add("Consultas semestrais obrigatórias");
                recomendacoes.add("Exames cardíacos semestrais (ecocardiograma)");
                recomendacoes.add("Acompanhamento articular — avaliar dor e mobilidade");
                recomendacoes.add("Dieta específica para idosos (menor proteína, mais fibra)");
                recomendacoes.add("Exames de sangue e urina a cada 6 meses");
            } else if (especie.contains("gato") || especie.contains("felino")) {
                recomendacoes.add("Monitoramento renal semestral");
                recomendacoes.add("Avaliação da tireoide (hipertireoidismo é comum em gatos idosos)");
                recomendacoes.add("Dieta para suporte renal e articular");
                recomendacoes.add("Consultas semestrais");
            } else {
                recomendacoes.add("Acompanhamento geriátrico semestral");
                recomendacoes.add("Exames preventivos completos a cada 6 meses");
            }
        } else {
            recomendacoes.add("Agendar consulta para avaliação da fase de vida atual");
        }

        return RecomendacaoResponseDTO.builder()
                .idPet(id)
                .nomePet(pet.getNome())
                .especie(pet.getEspecie() != null ? pet.getEspecie().getNome() : null)
                .faseVida(faseVida)
                .idadeMeses(idadeMeses)
                .recomendacoes(recomendacoes)
                .build();
    }

    @Transactional(readOnly = true)
    public Pet buscarEntidade(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + id));
    }

    private void preencherPet(Pet pet, PetRequestDTO dto) {
        Tutor tutor = tutorRepository.findById(dto.getIdTutor())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tutor não encontrado com ID: " + dto.getIdTutor()));

        Especie especie = especieRepository.findById(dto.getIdEspecie())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Espécie não encontrada com ID: " + dto.getIdEspecie()));

        pet.setNome(dto.getNome());
        pet.setDataNascimento(dto.getDataNascimento());
        pet.setSexo(dto.getSexo());
        pet.setPesoAtual(dto.getPesoAtual());
        pet.setMicrochip(dto.getMicrochip());
        pet.setFotoUrl(dto.getFotoUrl());
        pet.setTutor(tutor);
        pet.setEspecie(especie);

        if (dto.getIdRaca() != null) {
            Raca raca = racaRepository.findById(dto.getIdRaca())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Raça não encontrada com ID: " + dto.getIdRaca()));
            // Regra: raça deve pertencer à espécie informada
            if (!raca.getEspecie().getIdEspecie().equals(dto.getIdEspecie())) {
                throw new RegraNegocioException("A raça informada não pertence à espécie selecionada.");
            }
            pet.setRaca(raca);
        }
    }

    public int calcularIdadeMeses(Pet pet) {
        if (pet.getDataNascimento() == null) return 0;
        return Period.between(pet.getDataNascimento(), LocalDate.now()).toTotalMonths() > 0
                ? (int) Period.between(pet.getDataNascimento(), LocalDate.now()).toTotalMonths()
                : 0;
    }

    public String calcularFaseVida(Pet pet, int idadeMeses) {
        if (pet.getEspecie() == null) return "Desconhecida";
        return faseVidaRepository
                .findFaseAtual(pet.getEspecie().getIdEspecie(), idadeMeses)
                .map(FaseVida::getNome)
                .orElse("Desconhecida");
    }

    private PetResponseDTO toResponseDTO(Pet pet) {
        int idadeMeses = calcularIdadeMeses(pet);
        String faseVida = calcularFaseVida(pet, idadeMeses);

        return PetResponseDTO.builder()
                .idPet(pet.getIdPet())
                .nome(pet.getNome())
                .dataNascimento(pet.getDataNascimento())
                .idadeMeses(idadeMeses)
                .faseVida(faseVida)
                .sexo(pet.getSexo())
                .pesoAtual(pet.getPesoAtual())
                .microchip(pet.getMicrochip())
                .fotoUrl(pet.getFotoUrl())
                .dataCadastro(pet.getDataCadastro())
                .nomeTutor(pet.getTutor() != null ? pet.getTutor().getNome() : null)
                .idTutor(pet.getTutor() != null ? pet.getTutor().getIdTutor() : null)
                .especie(pet.getEspecie() != null ? pet.getEspecie().getNome() : null)
                .raca(pet.getRaca() != null ? pet.getRaca().getNome() : null)
                .build();
    }
}
