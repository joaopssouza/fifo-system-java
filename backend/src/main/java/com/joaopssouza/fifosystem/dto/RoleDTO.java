package com.joaopssouza.fifosystem.dto;

import java.util.Set;

public record RoleDTO(
    Long id,
    String name,
    String description,
    Set<String> permissions // Mandamos apenas os nomes das permissões, mais leve
) {}