package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.Role;
import com.joaopssouza.fifosystem.domain.entity.User;
import com.joaopssouza.fifosystem.domain.repository.RoleRepository;
import com.joaopssouza.fifosystem.domain.repository.UserRepository;
import com.joaopssouza.fifosystem.dto.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve criar usuário com sucesso quando dados são válidos")
    void shouldCreateUserSuccessfully() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("João Silva", "joao", "123456", "fifo", "OPERAÇÕES");
        Role mockRole = new Role();
        mockRole.setName("fifo");

        when(userRepository.findByUsername("joao")).thenReturn(Optional.empty());
        when(roleRepository.findByName("fifo")).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode("123456")).thenReturn("encoded_hash_123");

        // Act
        userService.createUser(request);

        // Assert
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getUsername().equals("joao") &&
            user.getFullName().equals("João Silva") &&
            user.getPasswordHash().equals("encoded_hash_123") &&
            user.getRole().getName().equals("fifo")
        ));
    }

    @Test
    @DisplayName("Não deve permitir criar usuário duplicado")
    void shouldThrowErrorWhenUserExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("João Silva", "joao", "123456", "fifo", "OPERAÇÕES");
        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(new User())); // Usuário já existe

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request);
        });

        assertEquals("Usuário já existe.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar se o papel (Role) não existir")
    void shouldThrowErrorWhenRoleNotFound() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("João", "joao", "123", "superadmin", "TI");
        when(userRepository.findByUsername("joao")).thenReturn(Optional.empty());
        when(roleRepository.findByName("superadmin")).thenReturn(Optional.empty()); // Role não existe

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request);
        });
    }

    @Test
    @DisplayName("Deve resetar a senha corretamente")
    void shouldResetPasswordSuccessfully() {
        // Arrange
        Long userId = 1L;
        String newPass = "novaSenha123";
        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPass)).thenReturn("new_encoded_hash");

        // Act
        userService.resetPassword(userId, newPass);

        // Assert
        verify(userRepository).save(argThat(user -> 
            user.getPasswordHash().equals("new_encoded_hash")
        ));
    }
}