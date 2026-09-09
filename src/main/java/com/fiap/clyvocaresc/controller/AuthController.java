package com.fiap.clyvocaresc.controller;

import com.fiap.clyvocaresc.dto.request.LoginRequestDTO;
import com.fiap.clyvocaresc.dto.request.RegisterOwnerRequestDTO;
import com.fiap.clyvocaresc.dto.request.RegisterVeterinarianRequestDTO;
import com.fiap.clyvocaresc.dto.response.AuthResponseDTO;
import com.fiap.clyvocaresc.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Porta de entrada da autenticação. É o único controller com rotas 100% públicas
 * (sem token) — todo o resto da API depende de um token emitido aqui. Owner e
 * Veterinarian não têm endpoint de criação nos próprios controllers de propósito:
 * o cadastro deles sempre passa por aqui, porque precisa nascer junto com o User
 * numa transação só (regra do FK obrigatório user_id).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Cadastra um novo tutor (User + Owner), retornando 201 sem corpo. */
    @PostMapping("/register/owner")
    public ResponseEntity<Void> registerOwner(@Valid @RequestBody RegisterOwnerRequestDTO req) {
        authService.registerOwner(req);
        return ResponseEntity.status(201).build();
    }

    /** Cadastra um novo veterinário (User + Veterinarian), retornando 201 sem corpo. */
    @PostMapping("/register/veterinarian")
    public ResponseEntity<Void> registerVeterinarian(@Valid @RequestBody RegisterVeterinarianRequestDTO req) {
        authService.registerVeterinarian(req);
        return ResponseEntity.status(201).build();
    }

    /** Autentica username/password e devolve o token JWT junto com o role do usuário. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
