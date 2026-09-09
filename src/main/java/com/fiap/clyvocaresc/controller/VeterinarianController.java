package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.response.VeterinarianResponseDTO;
import com.fiap.clyvocaresc.dto.request.VeterinarianUpdateRequestDTO;
import com.fiap.clyvocaresc.service.VeterinarianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe o perfil do veterinário e a listagem pública usada pelo tutor pra escolher com quem agendar. */
@RestController
@RequestMapping("/api/veterinarians")
@RequiredArgsConstructor
public class VeterinarianController {

    private final VeterinarianService veterinarianService;

    /** Lista todos os veterinários — usado pelo tutor na tela de agendamento. */
    @GetMapping
    public List<VeterinarianResponseDTO> findAll() {
        return veterinarianService.findAll();
    }

    /** Busca um veterinário por id. */
    @GetMapping("/{id}")
    public VeterinarianResponseDTO findById(@PathVariable Long id) {
        return veterinarianService.findById(id);
    }

    /** Retorna o perfil do veterinário autenticado. */
    @GetMapping("/me")
    public VeterinarianResponseDTO getMyProfile(Authentication authentication) {
        return veterinarianService.findByUsername(authentication.getName());
    }

    /** Atualiza o perfil do veterinário autenticado. */
    @PutMapping("/me")
    public VeterinarianResponseDTO updateMyProfile(Authentication authentication, @Valid @RequestBody VeterinarianUpdateRequestDTO dto) {
        return veterinarianService.updateOwnProfile(authentication.getName(), dto);
    }
}
