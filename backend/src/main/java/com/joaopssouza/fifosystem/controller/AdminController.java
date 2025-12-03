package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.dto.CreateUserRequest;
import com.joaopssouza.fifosystem.dto.UserDTO;
import com.joaopssouza.fifosystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // Apenas ADMIN pode ver usuários (Exemplo de segurança por anotação)
    // Para funcionar, adicione @EnableMethodSecurity na SecurityConfig
    // Ou configure no SecurityConfig via requestMatchers
    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return userService.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            userService.createUser(request);
            return ResponseEntity.ok(Map.of("message", "Utilizador criado com sucesso."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String fullName = (String) body.get("fullName");
            String sector = (String) body.get("sector");
            // O JSON pode mandar roleId como Integer, converter com segurança
            Number roleIdNum = (Number) body.get("roleId");
            Long roleId = roleIdNum != null ? roleIdNum.longValue() : null;

            userService.updateUser(id, fullName, roleId, sector);
            return ResponseEntity.ok(Map.of("message", "Utilizador atualizado com sucesso."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String newPassword = body.get("newPassword");
            if (newPassword == null || newPassword.length() < 6) {
                throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres.");
            }
            
            userService.resetPassword(id, newPassword);
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}