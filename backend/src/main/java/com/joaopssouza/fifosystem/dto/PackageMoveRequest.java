package com.joaopssouza.fifosystem.dto;

import jakarta.validation.constraints.NotBlank;

public record PackageMoveRequest(
    @NotBlank(message = "A nova rua é obrigatória")
    String rua
) {}