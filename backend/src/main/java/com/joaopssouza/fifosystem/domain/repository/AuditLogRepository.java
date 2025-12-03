package com.joaopssouza.fifosystem.domain.repository;

import com.joaopssouza.fifosystem.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    // Sem métodos manuais, o JpaSpecificationExecutor cuida de tudo.
}