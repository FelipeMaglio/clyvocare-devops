package br.com.clyvocare.clyvocare_api.service;

import br.com.clyvocare.clyvocare_api.dto.LembreteRequestDTO;
import br.com.clyvocare.clyvocare_api.dto.LembreteResponseDTO;
import br.com.clyvocare.clyvocare_api.entity.Lembrete;
import br.com.clyvocare.clyvocare_api.entity.Pet;
import br.com.clyvocare.clyvocare_api.entity.Tutor;
import br.com.clyvocare.clyvocare_api.exception.EntidadeNaoEncontradaException;
import br.com.clyvocare.clyvocare_api.exception.RegraNegocioException;
import br.com.clyvocare.clyvocare_api.repository.LembreteRepository;
import br.com.clyvocare.clyvocare_api.repository.PetRepository;
import br.com.clyvocare.clyvocare_api.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LembreteService {

    private final LembreteRepository lembreteRepository;
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    @Transactional
    @CacheEvict(value = "lembretes", allEntries = true)
    public LembreteResponseDTO criar(LembreteRequestDTO dto) {
        Pet pet = petRepository.findById(dto.getIdPet())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + dto.getIdPet()));

        Tutor tutor = tutorRepository.findById(dto.getIdTutor())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tutor não encontrado com ID: " + dto.getIdTutor()));

        // Regra: tutor deve ser o dono do pet
        if (!pet.getTutor().getIdTutor().equals(tutor.getIdTutor())) {
            throw new RegraNegocioException("O tutor informado não é responsável pelo pet informado.");
        }

        Lembrete lembrete = new Lembrete();
        lembrete.setPet(pet);
        lembrete.setTutor(tutor);
        lembrete.setTipo(dto.getTipo());
        lembrete.setDataEvento(dto.getDataEvento());
        lembrete.setMensagem(dto.getMensagem());
        lembrete.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDENTE");
        lembrete.setCanal(dto.getCanal());

        return toResponseDTO(lembreteRepository.save(lembrete));
    }
    @Transactional(readOnly = true)
    @Cacheable("lembretes")
    public Page<LembreteResponseDTO> listar(Pageable pageable) {
        return lembreteRepository.findAll(pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public LembreteResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }
    @Transactional(readOnly = true)
    public Page<LembreteResponseDTO> buscarPorPet(Long idPet, Pageable pageable) {
        petRepository.findById(idPet)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + idPet));
        return lembreteRepository.findByPetIdPet(idPet, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<LembreteResponseDTO> buscarPorTutor(Long idTutor, Pageable pageable) {
        tutorRepository.findById(idTutor)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tutor não encontrado com ID: " + idTutor));
        return lembreteRepository.findByTutorIdTutor(idTutor, pageable).map(this::toResponseDTO);
    }
    @Transactional(readOnly = true)
    public Page<LembreteResponseDTO> buscarPendentes(Pageable pageable) {
        return lembreteRepository.findPendentesAEnviar(pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "lembretes", allEntries = true)
    public LembreteResponseDTO atualizar(Long id, LembreteRequestDTO dto) {
        Lembrete lembrete = buscarEntidade(id);

        Pet pet = petRepository.findById(dto.getIdPet())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pet não encontrado com ID: " + dto.getIdPet()));

        Tutor tutor = tutorRepository.findById(dto.getIdTutor())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Tutor não encontrado com ID: " + dto.getIdTutor()));

        if (!pet.getTutor().getIdTutor().equals(tutor.getIdTutor())) {
            throw new RegraNegocioException("O tutor informado não é responsável pelo pet informado.");
        }

        lembrete.setPet(pet);
        lembrete.setTutor(tutor);
        lembrete.setTipo(dto.getTipo());
        lembrete.setDataEvento(dto.getDataEvento());
        lembrete.setMensagem(dto.getMensagem());
        lembrete.setStatus(dto.getStatus());
        lembrete.setCanal(dto.getCanal());

        return toResponseDTO(lembreteRepository.save(lembrete));
    }

    @Transactional
    @CacheEvict(value = "lembretes", allEntries = true)
    public void deletar(Long id) {
        lembreteRepository.delete(buscarEntidade(id));
    }

    public Lembrete buscarEntidade(Long id) {
        return lembreteRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Lembrete não encontrado com ID: " + id));
    }

    private LembreteResponseDTO toResponseDTO(Lembrete l) {
        boolean vencido = l.getDataEvento().isBefore(LocalDate.now())
                && "PENDENTE".equals(l.getStatus());

        return LembreteResponseDTO.builder()
                .idLembrete(l.getIdLembrete())
                .tipo(l.getTipo())
                .dataEvento(l.getDataEvento())
                .mensagem(l.getMensagem())
                .status(l.getStatus())
                .canal(l.getCanal())
                .vencido(vencido)
                .idPet(l.getPet().getIdPet())
                .nomePet(l.getPet().getNome())
                .idTutor(l.getTutor().getIdTutor())
                .nomeTutor(l.getTutor().getNome())
                .build();
    }
}
