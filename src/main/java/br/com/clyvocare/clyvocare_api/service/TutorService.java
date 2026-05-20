package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.TutorRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.TutorResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Cidade;
import br.com.clyvocare.clyvocare_api.entity.Tutor;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.CidadeRepository;
import br.com.clyvocare.clyvocare_api.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;
    private final CidadeRepository cidadeRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Transactional
    @CacheEvict(value = "tutores", allEntries = true)
    public TutorResponseDTO criar(TutorRequestDTO dto) {
        //  e-mail único
        if (tutorRepository.existsByEmail(dto.getEmail())) {
            throw new RegraNegocioException("Já existe um tutor cadastrado com o e-mail: " + dto.getEmail());
        }
        // CPF único
        if (tutorRepository.existsByCpf(dto.getCpf())) {
            throw new RegraNegocioException("Já existe um tutor cadastrado com o CPF informado.");
        }

        Tutor tutor = new Tutor();
        tutor.setNome(dto.getNome());
        tutor.setEmail(dto.getEmail());
        tutor.setTelefone(dto.getTelefone());
        tutor.setCpf(dto.getCpf());
        //  senha sempre armazenada como hash bcrypt
        tutor.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        tutor.setDataCadastro(LocalDate.now());

        if (dto.getIdCidade() != null) {
            Cidade cidade = cidadeRepository.findById(dto.getIdCidade())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Cidade não encontrada com ID: " + dto.getIdCidade()));
            tutor.setCidade(cidade);
        }

        return toResponseDTO(tutorRepository.save(tutor));
    }


    @Cacheable("tutores")
    public Page<TutorResponseDTO> listar(Pageable pageable) {
        return tutorRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<TutorResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return tutorRepository
                .findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorResponseDTO> buscarPorEstado(String estado, Pageable pageable) {
        return tutorRepository
                .findByEstado(estado.toUpperCase(), pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "tutores", allEntries = true)
    public TutorResponseDTO atualizar(Long id, TutorRequestDTO dto) {
        Tutor tutor = buscarEntidade(id);

        //  e-mail único (exceto o próprio)
        if (tutorRepository.existsByEmailAndIdTutorNot(dto.getEmail(), id)) {
            throw new RegraNegocioException("Já existe outro tutor cadastrado com o e-mail: " + dto.getEmail());
        }
        //  CPF único (exceto o próprio)
        if (tutorRepository.existsByCpfAndIdTutorNot(dto.getCpf(), id)) {
            throw new RegraNegocioException("Já existe outro tutor cadastrado com o CPF informado.");
        }

        tutor.setNome(dto.getNome());
        tutor.setEmail(dto.getEmail());
        tutor.setTelefone(dto.getTelefone());
        tutor.setCpf(dto.getCpf());

        //  só atualiza a senha se uma nova for enviada
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            tutor.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        }

        if (dto.getIdCidade() != null) {
            Cidade cidade = cidadeRepository.findById(dto.getIdCidade())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Cidade não encontrada com ID: " + dto.getIdCidade()));
            tutor.setCidade(cidade);
        }

        return toResponseDTO(tutorRepository.save(tutor));
    }


    @Transactional
    @CacheEvict(value = "tutores", allEntries = true)
    public void deletar(Long id) {
        Tutor tutor = buscarEntidade(id);
        // Regra: não permite deletar tutor com pets cadastrados
        if (tutor.getPets() != null && !tutor.getPets().isEmpty()) {
            throw new RegraNegocioException(
                    "Não é possível excluir o tutor pois ele possui " +
                    tutor.getPets().size() + " pet(s) cadastrado(s).");
        }
        tutorRepository.delete(tutor);
    }
    @Transactional(readOnly = true)
    public Tutor buscarEntidade(Long id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tutor não encontrado com ID: " + id));
    }

    private TutorResponseDTO toResponseDTO(Tutor tutor) {
        return TutorResponseDTO.builder()
                .idTutor(tutor.getIdTutor())
                .nome(tutor.getNome())
                .email(tutor.getEmail())
                .telefone(tutor.getTelefone())
                .cpf(tutor.getCpf())
                .dataCadastro(tutor.getDataCadastro())
                .nomeCidade(tutor.getCidade() != null ? tutor.getCidade().getNome() : null)
                .estado(tutor.getCidade() != null ? tutor.getCidade().getEstado() : null)
                .build();
    }
}
