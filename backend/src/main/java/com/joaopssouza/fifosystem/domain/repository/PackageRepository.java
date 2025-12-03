package com.joaopssouza.fifosystem.domain.repository;

import com.joaopssouza.fifosystem.domain.entity.ProductPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PackageRepository extends JpaRepository<ProductPackage, Long> {

    // --- NOVO MÉTODO ---
    // Busca todos os pacotes cujo buffer NÃO é o valor passado (ex: "PENDENTE")
    List<ProductPackage> findByBufferNotOrderByEntryTimestampAsc(String buffer);

    // Spring Data JPA cria a query automaticamente baseada no nome do método!
    List<ProductPackage> findByBuffer(String buffer);

    boolean existsByTrackingId(String trackingId);

    // Busca ignorando o Soft Delete (Native Query)
    @Query(value = "SELECT COUNT(*) > 0 FROM packages WHERE tracking_id = :trackingId", nativeQuery = true)
    boolean existsByTrackingIdGlobally(String trackingId);

    // Busca pacote mesmo se deletado (para reimpressão)
    @Query(value = "SELECT * FROM packages WHERE tracking_id = :trackingId LIMIT 1", nativeQuery = true)
    ProductPackage findByTrackingIdGlobally(String trackingId);
}