package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.ProductPackage;
import com.joaopssouza.fifosystem.domain.repository.PackageRepository;
import com.joaopssouza.fifosystem.dto.PackageEntryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

    @Mock // Cria uma versão "falsa" do repositório
    private PackageRepository packageRepository;

    @Mock // Cria uma versão "falsa" do serviço de auditoria
    private AuditService auditService;

    @InjectMocks // Injeta os mocks acima dentro do PackageService real
    private PackageService packageService;

    @Test
    @DisplayName("Deve registrar entrada com sucesso quando dados são válidos")
    void shouldRegisterEntrySuccessfully() {
        // 1. Arrange (Preparar o cenário)
        PackageEntryRequest request = new PackageEntryRequest("CG001", "RTS", "RUA-01", "P");
        
        // Quando o serviço perguntar se existe, o mock diz "NÃO"
        when(packageRepository.existsByTrackingId("CG001")).thenReturn(false);
        
        // Quando o serviço tentar salvar, o mock devolve o objeto que recebeu
        when(packageRepository.save(any(ProductPackage.class))).thenAnswer(invocation -> {
            ProductPackage p = invocation.getArgument(0);
            p.setId(1L); // Simula o ID gerado pelo banco
            return p;
        });

        // 2. Act (Executar a ação)
        ProductPackage result = packageService.registerEntry(request);

        // 3. Assert (Verificar o resultado)
        assertNotNull(result);
        assertEquals("CG001", result.getTrackingId());
        assertEquals(250, result.getProfileValue()); // Garante que a lógica do switch funcionou (P=250)
        
        // Verificação Crítica: Garante que o log de auditoria FOI chamado
        verify(auditService, times(1)).logAction(eq("ENTRADA"), contains("CG001"));
    }

    @Test
    @DisplayName("Não deve permitir Tracking ID duplicado")
    void shouldThrowErrorWhenDuplicateTrackingId() {
        // 1. Arrange
        PackageEntryRequest request = new PackageEntryRequest("CG001", "RTS", "RUA-01", "P");
        
        // Simula que o ID JÁ EXISTE no banco
        when(packageRepository.existsByTrackingId("CG001")).thenReturn(true);

        // 2. Act & Assert (Verificar se lança a exceção correta)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            packageService.registerEntry(request);
        });

        assertEquals("O Tracking ID CG001 já está na fila.", exception.getMessage());

        // 3. Verify (Garante que NADA foi salvo no banco e NENHUM log foi gerado)
        verify(packageRepository, never()).save(any());
        verify(auditService, never()).logAction(anyString(), anyString());
    }
}