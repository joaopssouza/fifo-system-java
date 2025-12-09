package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.AuditLog;
import com.joaopssouza.fifosystem.domain.entity.User;
import com.joaopssouza.fifosystem.domain.repository.AuditLogRepository;
import com.joaopssouza.fifosystem.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // Propagation.REQUIRES_NEW: Garante que o log seja salvo mesmo se a transação principal falhar
    // (Ou use REQUIRED padrão se quiser que o log falhe junto com a operação)
    @Transactional(propagation = Propagation.REQUIRED)
    public void logAction(String action, String details) {
        String username = "Sistema"; // Fallback
        String fullname = "Automático";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            // Tenta buscar o Full Name no repositório de usuários
            userRepository.findByUsername(username).ifPresent(user -> {
                if (user.getFullName() != null && !user.getFullName().isBlank()) {
                    // Seta o fullName real
                    // Usamos array/holder para modificar variável local dentro do lambda
                }
            });
            // Se não encontrado, mantém fallback com username
            fullname = userRepository.findByUsername(username).map(User::getFullName).orElse(username);
        }

        AuditLog log = AuditLog.builder()
                .username(username)
                .userFullname(fullname)
                .action(action)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }
}