package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.dto.JwtAuthenticationResponse;
import com.joaopssouza.fifosystem.dto.LoginRequest;
import com.joaopssouza.fifosystem.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.joaopssouza.fifosystem.domain.entity.User;
import com.joaopssouza.fifosystem.domain.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository; // Injeção nova

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        // 1. Autentica
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Busca o usuário COMPLETO com Roles e Permissions
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new RuntimeException("Erro: Usuário não encontrado após autenticação."));

        // 3. Gera o Token "recheado"
        String jwt = tokenProvider.generateToken(user);

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }
}