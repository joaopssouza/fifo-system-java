package com.joaopssouza.fifosystem.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank String fullName,
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String role, // Ex: "admin"
    @NotBlank String sector
) {}