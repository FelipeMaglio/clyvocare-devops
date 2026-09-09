package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.response.OwnerResponseDTO;
import com.fiap.clyvocaresc.dto.request.OwnerUpdateRequestDTO;
import com.fiap.clyvocaresc.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Expõe o perfil do tutor. As rotas "/me" usam o `Authentication` injetado pelo
 * Spring Security pra descobrir automaticamente quem está logado (via username do
 * token JWT) — o tutor nunca passa o próprio id na URL, evitando que ele tente
 * acessar/editar o perfil de outro tutor trocando o id manualmente.
 */
@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    /** Retorna o perfil do tutor autenticado. */
    @GetMapping("/me")
    public OwnerResponseDTO getMyProfile(Authentication authentication) {
        return ownerService.findByUsername(authentication.getName());
    }

    /** Atualiza o perfil do tutor autenticado. */
    @PutMapping("/me")
    public OwnerResponseDTO updateMyProfile(Authentication authentication, @Valid @RequestBody OwnerUpdateRequestDTO dto) {
        return ownerService.updateOwnProfile(authentication.getName(), dto);
    }

    /** Busca o perfil de um tutor por id — uso interno/administrativo (ex: veterinário consultando o dono do pet). */
    @GetMapping("/{id}")
    public OwnerResponseDTO findById(@PathVariable Long id) {
        return ownerService.findById(id);
    }
}
