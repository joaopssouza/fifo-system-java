package com.joaopssouza.fifosystem.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "packages") // Mapeia para a tabela criada na V2
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE packages SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL") // O Hibernate vai filtrar os deletados automaticamente nas buscas normais

public class ProductPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true)
    private String trackingId;

    @Column(nullable = false)
    private String buffer; // RTS, EHA, SAL

    @Column(nullable = false)
    private String rua;

    @Column(name = "profile_type")
    private String profileType; // P, M, G

    @Column(name = "profile_value")
    private Integer profileValue;

    @Column(name = "entry_timestamp")
    private LocalDateTime entryTimestamp;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}