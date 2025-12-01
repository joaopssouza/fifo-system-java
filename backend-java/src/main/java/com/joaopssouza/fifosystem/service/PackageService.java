package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.ProductPackage;
import com.joaopssouza.fifosystem.domain.repository.PackageRepository;
import com.joaopssouza.fifosystem.dto.PackageEntryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageService {

    private final PackageRepository packageRepository;
    private final AuditService auditService; // Injeção do serviço de auditoria

    // Agora lista apenas o que NÃO é PENDENTE (ou seja, itens reais na fila)
    public List<ProductPackage> findAll() {
        return packageRepository.findByBufferNot("PENDENTE");
    }

    // Regra de Negócio: Entrada de Pacote
    @Transactional // Garante que se o log ou o save falhar, tudo é desfeito (Rollback)
    public ProductPackage registerEntry(PackageEntryRequest request) {

        // 1. Validação: Tracking ID Duplicado
        if (packageRepository.existsByTrackingId(request.trackingId())) {
            throw new IllegalArgumentException("O Tracking ID " + request.trackingId() + " já está na fila.");
        }

        // 2. Lógica de Perfil (Profile Logic)
        String profileCode = "N/A";
        int profileValue = 0;

        // Se NÃO for "SAL" (Salvados), o perfil é obrigatório e tem valor
        if (!"SAL".equals(request.buffer())) {
            if (request.profile() == null) {
                throw new IllegalArgumentException("Perfil é obrigatório para buffers RTS e EHA.");
            }
            profileCode = request.profile();

            // Switch Expression (Feature do Java moderno)
            profileValue = switch (profileCode) {
                case "P" -> 250;
                case "M" -> 80;
                case "G" -> 10;
                default -> 0;
            };
        }

        // 3. Montagem da Entidade (Builder Pattern)
        ProductPackage newPackage = ProductPackage.builder()
                .trackingId(request.trackingId())
                .buffer(request.buffer())
                .rua(request.rua())
                .profileType(profileCode)
                .profileValue(profileValue)
                .entryTimestamp(LocalDateTime.now()) // Hora do servidor
                .build();

        // 4. Persistência
        ProductPackage savedPackage = packageRepository.save(newPackage);

        // 5. Auditoria Automática
        String details = String.format("Pacote %s entrou no buffer %s na rua %s (Perfil: %s)",
                savedPackage.getTrackingId(),
                savedPackage.getBuffer(),
                savedPackage.getRua(),
                savedPackage.getProfileType());

        auditService.logAction("ENTRADA", details);

        return savedPackage;
    }

    // Regra de Negócio: Saída de Pacote
    @Transactional
    public void registerExit(Long id) {
        // 1. Verificar existência antes de apagar
        ProductPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado com ID: " + id));

        // 2. Remover do banco
        packageRepository.deleteById(id);

        // 3. Auditoria Automática (usamos os dados do pacote recuperado antes de
        // apagar)
        String details = String.format("Pacote %s saiu do buffer %s",
                pkg.getTrackingId(), pkg.getBuffer());

        auditService.logAction("SAIDA", details);
    }

    @Transactional
    public ProductPackage registerMove(Long id, String newRua) {
        // 1. Buscar o pacote (lança erro se não existir)
        ProductPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado com ID: " + id));

        String oldRua = pkg.getRua();

        // 2. Verificar se houve mudança real (otimização)
        if (oldRua.equals(newRua)) {
            return pkg; // Nada a fazer
        }

        // 3. Atualizar o estado (O Hibernate gerencia o UPDATE automaticamente no final
        // da transação)
        pkg.setRua(newRua);
        // pkg.setUpdatedAt(LocalDateTime.now()); // O Hibernate já faz isso via
        // @UpdateTimestamp

        // 4. Auditoria
        String details = String.format("Pacote %s movido da rua %s para %s",
                pkg.getTrackingId(), oldRua, newRua);

        auditService.logAction("MOVIMENTACAO", details);

        return pkg;
    }

    // Método necessário para a compatibilidade com o Frontend antigo
    public ProductPackage findByTrackingIdGlobally(String trackingId) {
        return packageRepository.findByTrackingIdGlobally(trackingId);
    }
}