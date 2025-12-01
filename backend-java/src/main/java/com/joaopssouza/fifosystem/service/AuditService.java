package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.AuditLog;
import com.joaopssouza.fifosystem.domain.repository.AuditLogRepository;
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

    // Propagation.REQUIRES_NEW: Garante que o log seja salvo mesmo se a transação principal falhar
    // (Ou use REQUIRED padrão se quiser que o log falhe junto com a operação)
    @Transactional(propagation = Propagation.REQUIRED)
    public void logAction(String action, String details) {
        String username = "Sistema"; // Fallback
        String fullname = "Automático";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            // Em uma implementação real, buscaríamos o fullname do UserDetails ou do banco
            // Para simplificar agora, usamos o username ou um placeholder
            fullname = username; 
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