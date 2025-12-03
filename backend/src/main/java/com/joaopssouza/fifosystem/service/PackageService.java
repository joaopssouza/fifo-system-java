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
        return packageRepository.findByBufferNotOrderByEntryTimestampAsc("PENDENTE");
    }

    // Regra de Negócio: Entrada de Pacote
    @Transactional
    public ProductPackage registerEntry(PackageEntryRequest request) {
        
        // 1. Busca Global: Verifica se o ID existe (Ativo OU Deletado)
        ProductPackage pkg = packageRepository.findByTrackingIdGlobally(request.trackingId());

        // Se existe e está ATIVO (deletedAt == null), impede a entrada duplicada
        if (pkg != null && pkg.getDeletedAt() == null) {
            throw new IllegalArgumentException("O Tracking ID " + request.trackingId() + " já está na fila.");
        }

        // Lógica de Perfil (Extraída para reutilizar)
        String profileCode = "N/A";
        int profileValue = 0;

        if (!"SAL".equals(request.buffer())) {
            if (request.profile() == null) {
                throw new IllegalArgumentException("Perfil é obrigatório para buffers RTS e EHA.");
            }
            profileCode = request.profile();
            profileValue = switch (profileCode) {
                case "P" -> 250;
                case "M" -> 80;
                case "G" -> 10;
                default -> 0;
            };
        }

        // 2. Decisão: Criar Novo ou Reativar Existente?
        if (pkg == null) {
            // CENÁRIO A: Nunca existiu -> Cria um novo objeto
            pkg = ProductPackage.builder()
                    .trackingId(request.trackingId())
                    .createdAt(LocalDateTime.now()) // Apenas para novos
                    .build();
        } 
        
        // CENÁRIO B (Reativação) ou A (Continuação da criação):
        // Atualizamos os dados do objeto (seja ele novo ou recuperado do lixo)
        pkg.setBuffer(request.buffer());
        pkg.setRua(request.rua());
        pkg.setProfileType(profileCode);
        pkg.setProfileValue(profileValue);
        pkg.setEntryTimestamp(LocalDateTime.now());
        pkg.setDeletedAt(null); // <--- O Pulo do Gato: "Ressuscita" o item se estava deletado

        // 3. Persistência (Save funciona como Update se o ID já existe)
        ProductPackage savedPackage = packageRepository.save(pkg);

        // 4. Auditoria
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
