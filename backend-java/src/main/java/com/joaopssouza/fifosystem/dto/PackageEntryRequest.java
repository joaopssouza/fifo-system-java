package com.joaopssouza.fifosystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PackageEntryRequest(
    @NotBlank(message = "Tracking ID é obrigatório")
    String trackingId,

    @NotBlank(message = "Buffer é obrigatório")
    @Pattern(regexp = "RTS|EHA|SAL", message = "Buffer deve ser RTS, EHA ou SAL")
    String buffer,

    @NotBlank(message = "Rua é obrigatória")
    String rua,

    // Opcional, pois SAL não exige perfil
    @Pattern(regexp = "P|M|G|N/A", message = "Perfil inválido")
    String profile
) {}