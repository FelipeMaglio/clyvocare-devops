package com.fiap.clyvocaresc.service;

import com.fiap.clyvocaresc.dto.request.LoginRequestDTO;
import com.fiap.clyvocaresc.dto.request.RegisterOwnerRequestDTO;
import com.fiap.clyvocaresc.dto.request.RegisterVeterinarianRequestDTO;
import com.fiap.clyvocaresc.dto.response.AuthResponseDTO;
import com.fiap.clyvocaresc.entity.*;
import com.fiap.clyvocaresc.entity.enums.Role;
import com.fiap.clyvocaresc.repository.*;
import com.fiap.clyvocaresc.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Orquestra cadastro e login. registerOwner/registerVeterinarian criam User + a
 * entidade de perfil (Owner/Veterinarian) numa única transação, porque o FK
 * user_id é obrigatório nas duas — não dá pra existir um sem o outro. login()
 * delega a validação de senha pro AuthenticationManager do Spring Security e,
 * se a credencial bater, emite o token JWT que o resto da API vai exigir.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final CityRepository cityRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Cria User (role OWNER) + Owner numa transação só; falha se o username já existir.
     */
    @Transactional
    public void registerOwner(RegisterOwnerRequestDTO req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username já está em uso");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.OWNER);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Owner owner = new Owner();
        owner.setName(req.name());
        owner.setPhone(req.phone());
        owner.setDocument(req.document());
        owner.setRegisteredAt(LocalDate.now());
        owner.setUser(user);

        if (req.cityId() != null) {
            City city = cityRepository.findById(req.cityId())
                    .orElseThrow(() -> new IllegalArgumentException("Cidade não encontrada com id " + req.cityId()));
            owner.setCity(city);
        }

        ownerRepository.save(owner);
    }

    /**
     * Cria User (role VETERINARIAN) + Veterinarian numa transação só; falha se o username já existir.
     */
    @Transactional
    public void registerVeterinarian(RegisterVeterinarianRequestDTO req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username já está em uso");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.VETERINARIAN);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Veterinarian vet = new Veterinarian();
        vet.setName(req.name());
        vet.setLicenseNumber(req.licenseNumber());
        vet.setSpecialty(req.specialty());
        vet.setUser(user);

        if (req.clinicId() != null) {
            Clinic clinic = clinicRepository.findById(req.clinicId())
                    .orElseThrow(() -> new IllegalArgumentException("Clínica não encontrada com id " + req.clinicId()));
            vet.setClinic(clinic);
        }

        veterinarianRepository.save(vet);
    }

    /**
     * Valida username/password via AuthenticationManager e, se corretos, gera o token JWT com o role embutido.
     */
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return new AuthResponseDTO(token, role);
    }
}