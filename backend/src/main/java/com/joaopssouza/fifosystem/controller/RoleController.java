package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.domain.repository.RoleRepository;
import com.joaopssouza.fifosystem.dto.RoleDTO; // Importe o DTO
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/management/roles")
@RequiredArgsConstructor
@Tag(name = "Papéis", description = "Gestão de Roles e Permissões")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public Map<String, List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleRepository.findAll().stream()
                .map(role -> new RoleDTO(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        // Converte as permissões (Objetos) em uma lista de Strings simples
                        role.getPermissions().stream()
                                .map(p -> p.getName())
                                .collect(Collectors.toSet())
                ))
                .toList();

        return Map.of("data", roles);
    }
}