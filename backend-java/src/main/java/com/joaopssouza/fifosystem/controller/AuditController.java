package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.domain.entity.AuditLog;
import com.joaopssouza.fifosystem.domain.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            @RequestParam(required = false) String endDate
    ) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        // Converte string 'yyyy-MM-dd' para LocalDateTime
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        // Limpa strings vazias para null
        if (username != null && username.isEmpty()) username = null;
        if (fullname != null && fullname.isEmpty()) fullname = null;
        if (action != null && action.isEmpty()) action = null;

        return auditLogRepository.findWithFilters(username, fullname, action, start, end);
    }
}