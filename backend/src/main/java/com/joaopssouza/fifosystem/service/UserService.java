package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.Role;
import com.joaopssouza.fifosystem.domain.entity.User;
import com.joaopssouza.fifosystem.domain.repository.RoleRepository;
import com.joaopssouza.fifosystem.domain.repository.UserRepository;
import com.joaopssouza.fifosystem.dto.CreateUserRequest;
import com.joaopssouza.fifosystem.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(
                        u.getId(), 
                        u.getFullName(), 
                        u.getUsername(), 
                        u.getSector(),
                        u.getRole().getName(),
                        u.getRole().getId()
                ))
                .toList();
    }

    @Transactional
    public void createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Usuário já existe.");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new IllegalArgumentException("Papel não encontrado: " + request.role()));

        User user = User.builder()
                .fullName(request.fullName())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .sector(request.sector())
                .role(role)
                .build();

        userRepository.save(user);
    }

   @Transactional
    public void updateUser(Long id, String fullName, Long roleId, String sector) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (fullName != null && !fullName.isEmpty()) user.setFullName(fullName);
        if (sector != null && !sector.isEmpty()) user.setSector(sector);
        
        if (roleId != null) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Papel não encontrado."));
            user.setRole(role);
        }
        
        // O save não é estritamente necessário devido ao @Transactional, mas é boa prática explícita
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}