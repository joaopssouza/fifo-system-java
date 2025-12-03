package com.joaopssouza.fifosystem.dto;

import jakarta.validation.constraints.NotBlank;

// Record: Cria automaticamente construtor, getters, equals e hashCode.
// Clean Code puro!
public record LoginRequest(
    @NotBlank(message = "O usuário é obrigatório")
    String username,

    @NotBlank(message = "A senha é obrigatória")
    String password
) {}