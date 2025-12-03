package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.domain.entity.AuditLog;
import com.joaopssouza.fifosystem.domain.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/management/logs")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Histórico de atividades")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<AuditLog> getAllLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String fullname,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // 1. Inicialização Segura (Substitui o where(null))
        // Cria uma condição inicial "vazia" que é sempre verdadeira (1=1)
        Specification<AuditLog> spec = (root, query, cb) -> cb.conjunction(); // 2. Adicionar Filtros

        // Username
        if (username != null && !username.trim().isEmpty()) {
            spec = spec.and(
                    (root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }

        // Nome Completo
        if (fullname != null && !fullname.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("userFullname")),
                    "%" + fullname.toLowerCase() + "%"));
        }

        // Ação
        if (action != null && !action.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }

        // Data Início
        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start));
        }

        // Data Fim
        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), end));
        }

        // 3. Buscar com ordenação
        return auditLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}