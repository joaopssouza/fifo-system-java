package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.domain.entity.Role;
import com.joaopssouza.fifosystem.domain.repository.RoleRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/management/roles")
@RequiredArgsConstructor
@Tag(name = "Papéis", description = "Gestão de Roles e Permissões")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public Map<String, List<Role>> getAllRoles() {
        // O Front-end espera um objeto { data: [...] }
        return Map.of("data", roleRepository.findAll());
    }
}