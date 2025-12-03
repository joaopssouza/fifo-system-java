package com.joaopssouza.fifosystem.domain.repository;

import com.joaopssouza.fifosystem.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // O Spring Data JPA cria a query automaticamente baseada no nome do método!
    // SELECT * FROM roles WHERE name = ?
    Optional<Role> findByName(String name);
}